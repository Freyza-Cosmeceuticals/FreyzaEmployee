package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.ApiException
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.TravelPlanDto
import com.freyza.employee.data.network.dto.TravelPlanEntryDto
import com.freyza.employee.data.network.dto.TravelPlanMetricsResponse
import com.freyza.employee.data.network.dto.toDomain
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.TravelPlanMetrics
import com.freyza.employee.domain.repository.TravelPlanRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class TravelPlanRepositoryImpl(
  private val postgrest: Postgrest,
  private val serverTime: ServerTime,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
) : TravelPlanRepository {

  // Cache for the most recently fetched current travel plan
  private var currentPlanCache: TravelPlan? = null

  // Cache for travel plans keyed by their ID
  private val planCache = mutableMapOf<String, TravelPlan>()

  // Cache for specific day plan entries keyed by "tpId-yyyy-mm-dd"
  private val todayEntryCache = mutableMapOf<String, TravelPlanEntry?>()

  // Cache for complete entry lists keyed by travel plan ID
  private val entriesCache = mutableMapOf<String, List<TravelPlanEntry>>()

  companion object {
    const val TAG: String = "TravelPlanRepo"
  }

  override suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean,
    forceRefresh: Boolean,
  ): Result<TravelPlan?> {
    if (!forceRefresh && currentPlanCache != null && currentPlanCache?.employeeId == employeeId) {
      Logger.d(TAG, "Cache hit for current travel plan")
      return Result.Success(currentPlanCache)
    }

    return safeApiCall(TAG) {
      val today = serverTime.todayIn()
      val thisMonth = DateFormatter.format(
        LocalDate(year = today.year, month = today.month, day = 1),
        DateFormatter.FormattingType.MACHINE
      )

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying travelPlan for current employee and month: $thisMonth from network")

        val travelPlanDto = postgrest.from("travelPlan").select {
          filter {
            TravelPlanDto::employeeId eq employeeId
            TravelPlanDto::month eq thisMonth
          }
        }.decodeSingleOrNull<TravelPlanDto>()

        val plan = travelPlanDto?.toDomain()
        if (plan != null) {
          currentPlanCache = plan
          planCache[plan.id] = plan
        }

        plan
      }
    }
  }

  override suspend fun getTodayTravelPlanEntry(
    tpId: String,
    forceRefresh: Boolean,
  ): Result<TravelPlanEntry?> {
    val today = serverTime.todayIn()
    val thisDay = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)
    val cacheKey = "$tpId-$thisDay"

    if (!forceRefresh && todayEntryCache.containsKey(cacheKey)) {
      Logger.d(TAG, "Cache hit for today's travel plan entry")
      return Result.Success(todayEntryCache[cacheKey])
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(
          TAG, "Querying travelPlanEntry for current employee and day: $thisDay from network"
        )

        val travelPlanEntryDto = postgrest.from("travelPlanEntry").select {
          filter {
            TravelPlanEntryDto::tpId eq tpId
            TravelPlanEntryDto::date eq thisDay
          }
        }.decodeSingleOrNull<TravelPlanEntryDto>()

        val entry = travelPlanEntryDto?.toDomain()
        todayEntryCache[cacheKey] = entry

        entry
      }
    }
  }

  override suspend fun getTravelPlanEntries(
    tpId: String,
    forceRefresh: Boolean,
  ): Result<List<TravelPlanEntry>> {
    if (!forceRefresh && entriesCache.containsKey(tpId)) {
      Logger.d(TAG, "Cache hit for travel plan entries: $tpId")
      return Result.Success(entriesCache[tpId]!!)
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(
          TAG, "Querying travelPlanEntries for current employee and tpId: $tpId from network"
        )

        val travelPlanEntriesDto = postgrest.from("travelPlanEntry").select {
          filter {
            TravelPlanEntryDto::tpId eq tpId
          }
        }.decodeList<TravelPlanEntryDto>()

        val travelPlanEntries = travelPlanEntriesDto.map { it.toDomain() }
        entriesCache[tpId] = travelPlanEntries

        travelPlanEntries
      }
    }
  }

  override suspend fun getTravelPlan(
    id: String,
    forceRefresh: Boolean,
  ): Result<TravelPlan?> {
    if (!forceRefresh && planCache.containsKey(id)) {
      Logger.d(TAG, "Cache hit for travel plan ID: $id")
      return Result.Success(planCache[id])
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying travel plan ID: $id from network")
        val travelPlanDto = postgrest.from("travelPlan").select {
          filter { TravelPlanDto::id eq id }
        }.decodeSingleOrNull<TravelPlanDto>()

        val plan = travelPlanDto?.toDomain()
        if (plan != null) {
          planCache[id] = plan
        }

        plan
      }
    }
  }
}
