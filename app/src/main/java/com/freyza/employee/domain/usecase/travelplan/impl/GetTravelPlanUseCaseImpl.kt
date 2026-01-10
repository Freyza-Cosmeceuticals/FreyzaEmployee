package com.freyza.employee.domain.usecase.travelplan.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTravelPlanUseCaseImpl(val travelPlanRepository: TravelPlanRepository) :
    GetTravelPlanUseCase {
    override suspend fun execute(input: GetTravelPlanUseCase.Input): GetTravelPlanUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = travelPlanRepository.getTravelPlan(input.id)
            when (result) {
                is Result.Success -> {
                    GetTravelPlanUseCase.Output.Success(result.data)
                }

                is Result.Error -> {
                    GetTravelPlanUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetTravelPlanUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }
}
