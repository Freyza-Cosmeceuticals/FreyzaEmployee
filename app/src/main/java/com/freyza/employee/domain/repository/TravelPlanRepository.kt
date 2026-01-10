package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlan

interface TravelPlanRepository {
    suspend fun getCurrentTravelPlan(employeeId: String): Result<TravelPlan>
    suspend fun getTravelPlan(id: String): Result<TravelPlan>
}
