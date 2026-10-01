package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.TravelPlanMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TravelPlanRepository {
  suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean = false,
    forceRefresh: Boolean = false,
  ): Result<TravelPlan?>

  fun observeCurrentTravelPlan(employeeId: String, today: LocalDate): Flow<TravelPlan?>

  suspend fun getTodayTravelPlanEntry(
    tpId: String,
    forceRefresh: Boolean = false,
  ): Result<TravelPlanEntry?>

  fun observeTodayTravelPlanEntry(tpId: String, today: LocalDate): Flow<TravelPlanEntry?>

  suspend fun getTravelPlanEntries(
    tpId: String,
    forceRefresh: Boolean = false,
  ): Result<List<TravelPlanEntry>>

  suspend fun getTravelPlan(
    id: String,
    forceRefresh: Boolean = false,
  ): Result<TravelPlan?>

  suspend fun getTravelPlanMetrics(
    id: String,
    forceRefresh: Boolean = false,
  ): Result<TravelPlanMetrics>

  suspend fun refreshTravelPlan(employeeId: String, today: LocalDate): Result<Unit>

  fun invalidateMetricsCache(id: String? = null)

  fun clearCache()
}
