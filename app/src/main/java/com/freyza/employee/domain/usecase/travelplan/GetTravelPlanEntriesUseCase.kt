package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.repository.TravelPlanRepository

class GetTravelPlanEntriesUseCase(private val travelPlanRepository: TravelPlanRepository) {
  suspend operator fun invoke(tpId: String): Result<List<TravelPlanEntry>> {
    return when (val result = travelPlanRepository.getTravelPlanEntries(tpId)) {
      is Result.Success -> Result.Success(result.data ?: emptyList())
      else -> result
    }
  }
}
