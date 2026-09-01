package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.TravelPlanMetrics

interface TravelPlanRepository {
  suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean = false,
    forceRefresh: Boolean = false,
  ): Result<TravelPlan?>

  suspend fun getTodayTravelPlanEntry(
    tpId: String,
    forceRefresh: Boolean = false,
  ): Result<TravelPlanEntry?>

  suspend fun getTravelPlanEntries(
    tpId: String,
    forceRefresh: Boolean = false,
  ): Result<List<TravelPlanEntry>>

  suspend fun getTravelPlan(
    id: String,
    forceRefresh: Boolean = false,
  ): Result<TravelPlan?>
  fun clearCache()
}
