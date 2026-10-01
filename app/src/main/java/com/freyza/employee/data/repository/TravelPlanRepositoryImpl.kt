package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.ApiException
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.data.local.dao.TravelPlanDao
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toEntity
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class TravelPlanRepositoryImpl(
  private val postgrest: Postgrest,
  private val serverTime: ServerTime,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
  private val travelPlanDao: TravelPlanDao,
) : TravelPlanRepository {

  private val metricsCache = mutableMapOf<String, TravelPlanMetrics>()

  companion object {
    const val TAG: String = "TravelPlanRepo"
  }

  override fun clearCache() {
    Logger.d(TAG, "Clearing travel plan caches (Room DB managed)")
    metricsCache.clear()
  }

  override fun invalidateMetricsCache(id: String?) {
    if (id != null) {
      Logger.d(TAG, "Invalidating metrics cache for plan: $id")
      metricsCache.remove(id)
    } else {
      Logger.d(TAG, "Invalidating all metrics cache")
      metricsCache.clear()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun observeCurrentTravelPlan(employeeId: String, today: LocalDate): Flow<TravelPlan?> {
    val thisMonth = DateFormatter.format(
      LocalDate(year = today.year, month = today.month, day = 1),
      DateFormatter.FormattingType.MACHINE
    )
    return travelPlanDao.getTravelPlanFlow(employeeId, thisMonth).flatMapLatest { planEntity ->
      if (planEntity == null) {
        flowOf(null)
      } else {
        travelPlanDao.getTravelPlanEntriesFlow(planEntity.id).map { entryEntities ->
          planEntity.toDomain(entryEntities.map { it.toDomain() })
        }
      }
    }
  }

  override fun observeTodayTravelPlanEntry(tpId: String, today: LocalDate): Flow<TravelPlanEntry?> {
    return travelPlanDao.getTodayTravelPlanEntryFlow(tpId, today).map { it?.toDomain() }
  }

  override suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean,
    forceRefresh: Boolean,
  ): Result<TravelPlan?> {
    val today = serverTime.todayIn()
    val thisMonth = DateFormatter.format(
      LocalDate(year = today.year, month = today.month, day = 1),
      DateFormatter.FormattingType.MACHINE
    )

    val cachedPlan = travelPlanDao.getTravelPlan(employeeId, thisMonth)
    if (cachedPlan != null && !forceRefresh) {
      val entries = travelPlanDao.getTravelPlanEntries(cachedPlan.id).map { it.toDomain() }
      return Result.Success(cachedPlan.toDomain(entries))
    }

    refreshTravelPlan(employeeId, today)
    val freshPlan = travelPlanDao.getTravelPlan(employeeId, thisMonth)
    val freshEntries = if (freshPlan != null) {
      travelPlanDao.getTravelPlanEntries(freshPlan.id).map { it.toDomain() }
    } else {
      emptyList()
    }
    return Result.Success(freshPlan?.toDomain(freshEntries))
  }

  override suspend fun refreshTravelPlan(employeeId: String, today: LocalDate): Result<Unit> {
    val thisMonth = DateFormatter.format(
      LocalDate(year = today.year, month = today.month, day = 1),
      DateFormatter.FormattingType.MACHINE
    )

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        val travelPlanDto = postgrest.from("travelPlan").select {
          filter {
            TravelPlanDto::employeeId eq employeeId
            TravelPlanDto::month eq thisMonth
          }
        }.decodeSingleOrNull<TravelPlanDto>()

        if (travelPlanDto != null) {
          val planEntity = travelPlanDto.toDomain().toEntity()

          val entriesDto = postgrest.from("travelPlanEntry").select {
            filter {
              TravelPlanEntryDto::tpId eq planEntity.id
            }
          }.decodeList<TravelPlanEntryDto>()

          val entryEntities = entriesDto.map { it.toDomain().toEntity() }
          travelPlanDao.insertPlanWithEntries(planEntity, entryEntities)
        }
      }
    }
  }

  override suspend fun getTodayTravelPlanEntry(
    tpId: String,
    forceRefresh: Boolean,
  ): Result<TravelPlanEntry?> {
    val today = serverTime.todayIn()
    val cached = travelPlanDao.getTodayTravelPlanEntry(tpId, today)
    return if (cached != null && !forceRefresh) {
      Result.Success(cached.toDomain())
    } else {
      Result.Success(travelPlanDao.getTodayTravelPlanEntry(tpId, today)?.toDomain())
    }
  }

  override suspend fun getTravelPlanEntries(
    tpId: String,
    forceRefresh: Boolean,
  ): Result<List<TravelPlanEntry>> {
    val cached = travelPlanDao.getTravelPlanEntries(tpId)
    return if (cached.isNotEmpty() && !forceRefresh) {
      Result.Success(cached.map { it.toDomain() })
    } else {
      Result.Success(travelPlanDao.getTravelPlanEntries(tpId).map { it.toDomain() })
    }
  }

  override suspend fun getTravelPlan(
    id: String,
    forceRefresh: Boolean,
  ): Result<TravelPlan?> {
    val cached = travelPlanDao.getTravelPlanById(id)
    return if (cached != null && !forceRefresh) {
      val entries = travelPlanDao.getTravelPlanEntries(id).map { it.toDomain() }
      Result.Success(cached.toDomain(entries))
    } else {
      Result.Success(travelPlanDao.getTravelPlanById(id)?.let { plan ->
        val entries = travelPlanDao.getTravelPlanEntries(id).map { it.toDomain() }
        plan.toDomain(entries)
      })
    }
  }

  override suspend fun getTravelPlanMetrics(
    id: String,
    forceRefresh: Boolean,
  ): Result<TravelPlanMetrics> {
    if (!forceRefresh && metricsCache.containsKey(id)) {
      Logger.d(TAG, "Cache hit for travel plan metrics ID: $id")
      return Result.Success(metricsCache[id]!!)
    }

    return safeApiCall(TAG, auth) { token ->
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching metrics for travel plan: $id")

        val response = httpClient.get("${appConfig.apiUrl}/api/plans/$id/metrics") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val metricsResponse = response.body<TravelPlanMetricsResponse>()

          if (metricsResponse.success) {
            val metrics = metricsResponse.data.toDomain()
            metricsCache[id] = metrics
            metrics
          } else {
            throw Exception("Failed to fetch metrics")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw ApiException(response.status.value, error, error.message)
        }
      }
    }
  }
}
