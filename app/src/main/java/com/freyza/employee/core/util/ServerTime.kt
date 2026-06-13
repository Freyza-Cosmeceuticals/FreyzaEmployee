package com.freyza.employee.core.util

import android.os.SystemClock
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Constants
import com.freyza.employee.data.network.dto.ServerStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Server synchronized time singleton. Stores offset and provides accurate server time.
 */
class ServerTime(
  private val httpClient: HttpClient? = null,
  private val appConfig: AppConfig? = null
) {
  @Volatile
  private var serverOffset: Long = 0L

  @Volatile
  private var isSynced: Boolean = false

  companion object {
    private const val TAG = "ServerTime"
  }

  /**
   * Syncs time with server by making an API call and calculating rtt and offset.
   */
  suspend fun syncTime() {
    if (httpClient == null || appConfig == null) return
    try {
      withContext(Dispatchers.IO) {
        val startTime = SystemClock.elapsedRealtime()
        val response: ServerStatusDto = httpClient.get("${appConfig.apiUrl}/api/version").body()
        val endTime = SystemClock.elapsedRealtime()

        val rtt = endTime - startTime
        val serverInstant = Instant.parse(response.serverTime)

        // Account for network latency by assuming server time was measured at the midpoint of RTT
        val deviceMidpointMillis = startTime + (rtt / 2)
        setServerOffset(serverInstant.toEpochMilliseconds() - deviceMidpointMillis)
        Logger.d(TAG, "Time synced with server. RTT: $rtt ms, Offset: $serverOffset ms. Now is: ${now()}")
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to sync time via custom API: ${e.message}")
    }
  }

  fun setServerOffset(offset: Long) {
    serverOffset = offset
    isSynced = true
  }

  /**
   * Returns current time Instant
   */
  fun now(): Instant {
    if (!isSynced) {
      return Clock.System.now()
    }
    val currentTimeMillis = SystemClock.elapsedRealtime() + serverOffset
    return Instant.fromEpochMilliseconds(currentTimeMillis)
  }

  /**
   * Returns current time LocalDateTime
   */
  fun nowLocalDateTime(
    timeZone: TimeZone = TimeZone.of(Constants.TIMEZONE)
  ): LocalDateTime {
    return now().toLocalDateTime(timeZone)
  }

  /**
   * Returns current time LocalDate in the given timezone.
   */
  fun todayIn(
    timeZone: TimeZone = TimeZone.of(Constants.TIMEZONE)
  ): LocalDate {
    return now().toLocalDateTime(timeZone).date
  }
}
