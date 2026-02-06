package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

interface TravelPlanRepository {
  suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean = false,
  ): Result<TravelPlan>

  suspend fun getTodayTravelPlanEntry(tpId: String): Result<TravelPlanEntry>
  suspend fun getTravelPlanEntries(tpId: String): Result<List<TravelPlanEntry>>
  suspend fun getTravelPlan(id: String): Result<TravelPlan>
}
