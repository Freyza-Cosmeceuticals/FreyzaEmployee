package com.freyza.employee.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.IOException

class OfflineException(message: String = "No internet connection") : IOException(message)

interface NetworkMonitor {
  val isOnline: StateFlow<Boolean>
  val isCurrentlyConnected: Boolean
}

class ConnectivityManagerNetworkMonitor(
  private val context: Context,
) : NetworkMonitor {
  private val connectivityManager = context.getSystemService<ConnectivityManager>()

  override val isCurrentlyConnected: Boolean
    get() = connectivityManager?.isCurrentlyConnected() ?: false

  override val isOnline: StateFlow<Boolean> = callbackFlow {
    val connectivityManager = context.getSystemService<ConnectivityManager>()
    if (connectivityManager == null) {
      channel.trySend(false)
      channel.close()
      return@callbackFlow
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
      private val networks = mutableSetOf<Network>()

      override fun onAvailable(network: Network) {
        networks += network
        channel.trySend(true)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
      ) {
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
          networks += network
        } else {
          networks -= network
        }
        channel.trySend(networks.isNotEmpty())
      }

      override fun onLost(network: Network) {
        networks -= network
        channel.trySend(networks.isNotEmpty())
      }
    }

    val request =
      NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED).build()
    connectivityManager.registerNetworkCallback(request, callback)

    /**
     * Sends the initial connectivity state to the block.
     */
    channel.trySend(connectivityManager.isCurrentlyConnected())

    awaitClose {
      connectivityManager.unregisterNetworkCallback(callback)
    }
  }.distinctUntilChanged().stateIn(
    scope = CoroutineScope(Dispatchers.Default),
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = connectivityManager?.isCurrentlyConnected() ?: false
  )

  private fun ConnectivityManager.isCurrentlyConnected() =
    activeNetwork?.let(::getNetworkCapabilities)
      ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
}

class ApiException(
  val statusCode: Int,
  val errorBody: Any? = null,
  message: String = "API request failed with status $statusCode",
) : Exception(message)

fun Throwable.isConnectivityOrDnsException(): Boolean {
  var current: Throwable? = this
  while (current != null) {
    val className = current.javaClass.name
    if (current is OfflineException ||
      className.contains("UnknownHostException") ||
      className.contains("UnresolvedAddressException") ||
      className.contains("SocketTimeoutException") ||
      className.contains("ConnectTimeoutException") ||
      className.contains("ConnectException") ||
      className.contains("GaiException")
    ) {
      return true
    }

    val msg = current.message
    if (msg != null && (
        msg.contains("unable to resolve host", ignoreCase = true) ||
        msg.contains("unknownhost", ignoreCase = true) ||
        msg.contains("failed to connect", ignoreCase = true) ||
        msg.contains("connecttimeout", ignoreCase = true) ||
        msg.contains("EAI_NODATA", ignoreCase = true) ||
        msg.contains("No address associated with hostname", ignoreCase = true)
      )
    ) {
      return true
    }
    current = current.cause
  }
  return false
}

suspend fun <T> safeApiCall(TAG: String = "safeApiCall", apiCall: suspend () -> T): Result<T> {
  val span = Sentry.getSpan()?.startChild("http.client", TAG)

  return try {
    val result = apiCall()
    span?.status = SpanStatus.OK

    Result.Success(result)
  } catch (e: ApiException) {
    span?.status = SpanStatus.INTERNAL_ERROR
    Logger.w(TAG, "ApiException: ${e.statusCode} - ${e.message}")
    Result.Error(message = e.message ?: "Request failed", errorBody = e.errorBody)
  } catch (e: Exception) {
    if (e is OfflineException || e.isConnectivityOrDnsException()) {
      span?.status = SpanStatus.UNAVAILABLE
      Sentry.addBreadcrumb(Breadcrumb().apply {
        category = "network"
        message = "$TAG: Offline/Connectivity issue (${e.javaClass.simpleName}): ${e.message}"
      })

      Logger.d(TAG, "Offline/Connectivity issue (${e.javaClass.simpleName}): ${e.message}")
      Result.Error("Please check your internet connection.", errorBody = e)
    } else {
      span?.status = SpanStatus.INTERNAL_ERROR
      span?.throwable = e

      Sentry.captureException(e)

      Logger.e(TAG, e.message.toString())
      Result.Error(e.message ?: "An unknown error occurred")
    }
  } finally {
    span?.finish()
  }
}
