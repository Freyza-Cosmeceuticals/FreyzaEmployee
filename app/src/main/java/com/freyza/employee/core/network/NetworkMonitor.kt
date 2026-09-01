package com.freyza.employee.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import io.github.jan.supabase.auth.Auth
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.IOException

class OfflineException(message: String = "No internet connection") : IOException(message)

interface NetworkMonitor {
  /**
   * Combined flow of system connectivity and server reachability.
   */
  val isOnline: StateFlow<Boolean>
  val isCurrentlyConnected: Boolean

  /**
   * Signal that an API call failed due to connectivity/DNS.
   */
  fun reportFailure()

  /**
   * Signal that an API call succeeded, confirming reachability.
   */
  fun reportSuccess()

  companion object {
    private var _instance: NetworkMonitor? = null

    /**
     * Internal: Sets the global instance for safeApiCall usage.
     */
    fun register(monitor: NetworkMonitor) {
      _instance = monitor
    }

    /**
     * Internal: Provides the global monitor.
     */
    val instance: NetworkMonitor? get() = _instance
  }
}

class ConnectivityManagerNetworkMonitor(
  private val context: Context,
) : NetworkMonitor {
  private val connectivityManager = context.getSystemService<ConnectivityManager>()
  private val isServerReachable = MutableStateFlow(true)

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
        // Reset reachability on a fresh system connection
        isServerReachable.value = true
        channel.trySend(true)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
      ) {
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
          networks += network
          // System validated a connection, give reachability another chance
          isServerReachable.value = true
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

    isServerReachable.value = true
    channel.trySend(connectivityManager.isCurrentlyConnected())

    awaitClose {
      connectivityManager.unregisterNetworkCallback(callback)
    }
  }.combine(isServerReachable) { systemOnline, reachable ->
    systemOnline && reachable
  }.distinctUntilChanged().stateIn(
    scope = CoroutineScope(Dispatchers.Default),
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = (connectivityManager?.isCurrentlyConnected() ?: false)
  )

  override fun reportFailure() {
    isServerReachable.value = false
  }

  override fun reportSuccess() {
    isServerReachable.value = true
  }

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

/**
 * Executes a network/API call safely, wrapping errors into [Result] and reporting spans to Sentry.
 */
suspend fun <T> safeApiCall(
  tag: String = "safeApiCall",
  apiCall: suspend () -> T,
): Result<T> {
  return safeApiCallInternal(tag, apiCall)
}

/**
 * Executes an authenticated network/API call safely, retrieving the Supabase access token,
 * wrapping errors into [Result], and reporting spans to Sentry.
 */
suspend fun <T> safeApiCall(
  tag: String = "safeApiCall",
  auth: Auth,
  apiCall: suspend (token: String) -> T,
): Result<T> {
  return safeApiCallInternal(tag) {
    val token = auth.getAccessToken() ?: throw IllegalStateException("No authentication token found")
    apiCall(token)
  }
}

private suspend fun <T> safeApiCallInternal(
  tag: String,
  apiCall: suspend () -> T,
): Result<T> {
  val span = Sentry.getSpan()?.startChild("http.client", tag)
  val monitor = NetworkMonitor.instance

  return try {
    val result = apiCall()
    span?.status = SpanStatus.OK
    monitor?.reportSuccess()

    Result.Success(result)
  } catch (e: ApiException) {
    span?.status = SpanStatus.INTERNAL_ERROR
    Logger.w(tag, "ApiException: ${e.statusCode} - ${e.message}")
    Result.Error(message = e.message ?: "Request failed", errorBody = e.errorBody)
  } catch (e: CancellationException) {
    span?.status = SpanStatus.CANCELLED
    Logger.d(tag, "Coroutine was cancelled.")
    // MUST rethrow so Kotlin can safely destroy the coroutine
    throw e
  } catch (e: Exception) {
    if (e is OfflineException || e.isConnectivityOrDnsException()) {
      span?.status = SpanStatus.UNAVAILABLE
      monitor?.reportFailure()

      Sentry.addBreadcrumb(Breadcrumb().apply {
        category = "network"
        message = "$tag: Offline/Connectivity issue (${e.javaClass.simpleName}): ${e.message}"
      })

      Logger.d(tag, "Offline/Connectivity issue (${e.javaClass.simpleName}): ${e.message}")
      Result.Error("Please check your internet connection.", errorBody = e)
    } else {
      span?.status = SpanStatus.INTERNAL_ERROR
      span?.throwable = e

      Sentry.captureException(e)

      Logger.e(tag, e.message.toString())
      Result.Error(e.message ?: "An unknown error occurred")
    }
  } finally {
    span?.finish()
  }
}
