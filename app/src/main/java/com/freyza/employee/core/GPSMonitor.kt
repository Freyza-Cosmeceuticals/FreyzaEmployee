package com.freyza.employee.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.freyza.employee.core.util.Logger
import io.sentry.Breadcrumb
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GPSMonitor(private val context: Context) {

  companion object {
    const val TAG = "GPSMonitor"
  }

  init {
    Logger.d(TAG, "Init")
  }

  private val locationManager =
    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val _isGpsEnabled = MutableStateFlow(checkGpsState())
  val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled

  private val gpsReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
        Logger.d(TAG, "Providers changed, checking GPS state")
        _isGpsEnabled.value = checkGpsState()
        Logger.d(TAG, "GPS state: ${_isGpsEnabled.value}")
      }
    }
  }

  fun startMonitoring() {
    Sentry.addBreadcrumb(Breadcrumb().apply {
      message = "Started GPS monitoring"
    })
    Logger.d(TAG, "Starting GPS monitoring")

    context.registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
  }

  fun stopMonitoring() {
    Sentry.addBreadcrumb(Breadcrumb().apply {
      message = "Stopping GPS monitoring"
    })
    Logger.d(TAG, "Stopping GPS monitoring")

    context.unregisterReceiver(gpsReceiver)
  }

  private fun checkGpsState(): Boolean {
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
      LocationManager.NETWORK_PROVIDER
    )
  }
}
