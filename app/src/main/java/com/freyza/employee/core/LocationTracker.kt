package com.freyza.employee.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.await
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.sentry.Sentry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class LocationTracker(private val context: Context) {

  companion object {
    const val TAG = "LocationTracker"
  }

  private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
  private val locationMutex = Mutex()

  private var cachedLocation: Location? = null
  private var lastFetchTime: Long = 0L
  private val CACHE_DURATION_MS = 120.seconds.inWholeMilliseconds
  private val LOCATION_REQUEST_TIMEOUT = 4.seconds

  suspend fun getCurrentLocation(): Location? = locationMutex.withLock {
    val span = Sentry.getSpan()?.startChild("location.gps", "Acquire GPS Location")
    val now = System.currentTimeMillis()

    // 1. Return cache if valid
    if (cachedLocation != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
      Logger.d(TAG, "cache still fresh, reusing $cachedLocation")
      span?.finish()
      return@withLock cachedLocation
    }

    // 2. Check permission
    val hasGrantedFineLocationPermission = ContextCompat.checkSelfPermission(
      context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasGrantedCoarseLocationPermission = ContextCompat.checkSelfPermission(
      context, android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val isGpsEnabled =
      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.GPS_PROVIDER
      )

    if (!isGpsEnabled || !(hasGrantedFineLocationPermission || hasGrantedCoarseLocationPermission)) {
      Logger.w(TAG, "Location permissions not granted or GPS is not enabled")
      span?.status = io.sentry.SpanStatus.PERMISSION_DENIED
      span?.finish()
      return@withLock null
    }

    // 2. Fetch fresh with timeout
    try {
      Logger.d(TAG, "Fetching fresh location")

      val freshLocation = withTimeoutOrNull<Location>(LOCATION_REQUEST_TIMEOUT) {
        fusedLocationClient.getCurrentLocation(
          Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
        ).await()
      }

      if (freshLocation != null) {
        Logger.d(TAG, "Fetched fresh location: $freshLocation. Caching and returning")
        cachedLocation = freshLocation
        lastFetchTime = now
        span?.status = io.sentry.SpanStatus.OK
        cachedLocation
      } else {
        span?.status = io.sentry.SpanStatus.DEADLINE_EXCEEDED
        handleLocationFailure(Exception("Timeout waiting for fresh GPS lock $LOCATION_REQUEST_TIMEOUT"))
      }
    } catch (e: Exception) {
      span?.status = io.sentry.SpanStatus.INTERNAL_ERROR
      span?.throwable = e
      handleLocationFailure(e)
    } finally {
      span?.finish()
    }
  }

  @SuppressLint("MissingPermission")
  private suspend fun handleLocationFailure(exception: Exception): Location? {
    Logger.w(TAG, "Fresh fetch failed, triggering fallback: ${exception.message}")

    Sentry.captureException(exception) {
      it.setTag("location_strategy", "fresh_fetch_failed")
    }

    return try {
      val lastKnown = fusedLocationClient.lastLocation.await()
      if (lastKnown != null) {
        Logger.d(TAG, "using lastLocation ${lastKnown}")
        lastKnown

      } else {
        Logger.e(TAG, "lastLocation also is null. Returning cache.")
        Sentry.captureMessage("lastLocation also is null. Returning cache.")

        cachedLocation
      }
    } catch (fallbackException: Exception) {
      Logger.e(TAG, "Unable to fetch last location: ${fallbackException.message}")
      Sentry.captureException(fallbackException) {
        it.setTag("location_strategy", "last_location_fallback_failed")
      }
      cachedLocation
    }

  }
}
