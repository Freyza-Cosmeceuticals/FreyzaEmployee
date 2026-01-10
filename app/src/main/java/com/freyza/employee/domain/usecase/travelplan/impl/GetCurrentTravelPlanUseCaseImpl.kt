package com.freyza.employee.domain.usecase.travelplan.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentTravelPlanUseCaseImpl(val travelPlanRepository: TravelPlanRepository) :
    GetCurrentTravelPlanUseCase {

    override suspend fun execute(input: GetCurrentTravelPlanUseCase.Input): GetCurrentTravelPlanUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = travelPlanRepository.getCurrentTravelPlan(input.employeeId)) {
                is Result.Success -> {
                    GetCurrentTravelPlanUseCase.Output.Success(result.data)
                }

                is Result.Error -> {
                    GetCurrentTravelPlanUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetCurrentTravelPlanUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }

}
