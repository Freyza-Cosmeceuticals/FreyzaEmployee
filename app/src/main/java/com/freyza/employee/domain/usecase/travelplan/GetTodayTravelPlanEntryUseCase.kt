package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.repository.TravelPlanRepository

class GetTodayTravelPlanEntryUseCase(private val travelPlanRepository: TravelPlanRepository) {
  suspend operator fun invoke(tpId: String): Result<TravelPlanEntry> {
    return travelPlanRepository.getTodayTravelPlanEntry(tpId)
  }
}
