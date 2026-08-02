package com.freyza.employee.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
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

suspend fun <T> safeApiCall(TAG: String = "safeApiCall", apiCall: suspend () -> T): Result<T> {
  return try {
    Result.Success(apiCall())
  } catch (e: OfflineException) {
    Logger.d(TAG, "OfflineException: ${e.message.toString()}")
    Result.Error("Please check your internet connection.")
  } catch (e: Exception) {
    Logger.e(TAG, e.message.toString())
    Result.Error(e.message ?: "An unknown error occurred")
  }
}
