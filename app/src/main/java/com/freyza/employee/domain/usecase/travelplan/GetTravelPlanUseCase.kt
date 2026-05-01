package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.repository.TravelPlanRepository

class GetTravelPlanUseCase(private val travelPlanRepository: TravelPlanRepository) {
  suspend operator fun invoke(id: String): Result<TravelPlan?> {
    return travelPlanRepository.getTravelPlan(id)
  }
}
