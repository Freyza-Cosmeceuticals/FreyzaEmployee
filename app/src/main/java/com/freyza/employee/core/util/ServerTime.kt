package com.freyza.employee.core.util

import android.os.SystemClock
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Constants
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.data.local.dao.AppMetaDao
import com.freyza.employee.data.local.entities.AppMetaEntity
import com.freyza.employee.data.network.dto.ServerStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
  private val appConfig: AppConfig? = null,
  private val appMetaDao: AppMetaDao? = null,
) {
  @Volatile
  private var serverOffset: Long = 0L

  private val _isSynced = MutableStateFlow(false)
  val isSynced = _isSynced.asStateFlow()

  companion object {
    private const val TAG = "ServerTime"
  }

  init {
    appMetaDao?.let { dao ->
      CoroutineScope(Dispatchers.IO).launch {
        val meta = dao.getAppMeta()
        if (meta != null && meta.serverTimeOffset != 0L) {
          setServerOffset(meta.serverTimeOffset)
          Logger.d(TAG, "Restored stored serverOffset from app_meta: ${meta.serverTimeOffset} ms")
        }
      }
    }
  }

  /**
   * Syncs time with server by making an API call and calculating rtt and offset.
   */
  suspend fun syncTime(forceRefresh: Boolean = false) {
    val nowMs = System.currentTimeMillis()

    withContext(Dispatchers.IO) {
      val meta = appMetaDao?.getAppMeta() ?: AppMetaEntity()

      if (!forceRefresh && meta.lastTimeSyncAt > 0L && (nowMs - meta.lastTimeSyncAt) < Constants.TTL_TIME_SYNC_MS) {
        if (serverOffset == 0L && meta.serverTimeOffset != 0L) {
          setServerOffset(meta.serverTimeOffset)
        } else if (serverOffset != 0L) {
          _isSynced.value = true
        }
        Logger.d(TAG, "Skipping network time sync; stored offset is within TTL (5h)")
        return@withContext
      }

      if (httpClient == null || appConfig == null) return@withContext

      safeApiCall(TAG) {
        val startTime = SystemClock.elapsedRealtime()
        val response: ServerStatusDto = httpClient.get("${appConfig.apiUrl}/api/version").body()
        val endTime = SystemClock.elapsedRealtime()

        val rtt = endTime - startTime
        val serverInstant = Instant.parse(response.serverTime)

        // Account for network latency by assuming server time was measured at the midpoint of RTT
        val deviceMidpointMillis = startTime + (rtt / 2)
        val calculatedOffset = serverInstant.toEpochMilliseconds() - deviceMidpointMillis

        setServerOffset(calculatedOffset)

        appMetaDao?.upsertAppMeta(
          meta.copy(
            serverTimeOffset = calculatedOffset,
            lastTimeSyncAt = nowMs
          )
        )

        Logger.d(
          TAG,
          "Time synced with server. RTT: $rtt ms, Offset: $serverOffset ms. Now is: ${now()}"
        )
      }
    }
  }

  private fun setServerOffset(offset: Long) {
    serverOffset = offset
    _isSynced.value = true
  }

  /**
   * Returns current time Instant
   */
  fun now(): Instant {
    if (!_isSynced.value) {
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
