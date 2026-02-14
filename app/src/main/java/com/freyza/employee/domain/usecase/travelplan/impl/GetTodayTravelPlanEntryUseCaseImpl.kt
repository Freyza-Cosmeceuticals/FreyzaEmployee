package com.freyza.employee.domain.usecase.travelplan.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTodayTravelPlanEntryUseCaseImpl(val travelPlanRepository: TravelPlanRepository) :
  GetTodayTravelPlanEntryUseCase {

  override suspend fun execute(input: GetTodayTravelPlanEntryUseCase.Input): GetTodayTravelPlanEntryUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = travelPlanRepository.getTodayTravelPlanEntry(input.tpId)) {
        is Result.Success -> {
          GetTodayTravelPlanEntryUseCase.Output.Success(result.data)
        }

        is Result.Error -> {
          GetTodayTravelPlanEntryUseCase.Output.Failure(result.message ?: "")
        }

        else -> {
          GetTodayTravelPlanEntryUseCase.Output.Failure(result.message ?: "")
        }
      }
    }
  }

}
