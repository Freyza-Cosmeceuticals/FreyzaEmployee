package com.freyza.employee.domain.usecase.travelplan.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanEntriesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTravelPlanEntriesUseCaseImpl(val travelPlanRepository: TravelPlanRepository) :
  GetTravelPlanEntriesUseCase {

  override suspend fun execute(input: GetTravelPlanEntriesUseCase.Input): GetTravelPlanEntriesUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = travelPlanRepository.getTravelPlanEntries(input.tpId)) {
        is Result.Success -> {
          GetTravelPlanEntriesUseCase.Output.Success(result.data ?: listOf())
        }

        is Result.Error -> {
          GetTravelPlanEntriesUseCase.Output.Failure(result.message ?: "")
        }

        else -> {
          GetTravelPlanEntriesUseCase.Output.Failure(result.message ?: "")
        }
      }
    }
  }

}
