package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.usecase.UseCase

interface GetTravelPlanUseCase : UseCase<GetTravelPlanUseCase.Input, GetTravelPlanUseCase.Output> {
    class Input(val id: String)

    sealed class Output() {
        data class Success(val travelPlan: TravelPlan?) : Output()
        data class Failure(val message: String) : Output()
    }
}
