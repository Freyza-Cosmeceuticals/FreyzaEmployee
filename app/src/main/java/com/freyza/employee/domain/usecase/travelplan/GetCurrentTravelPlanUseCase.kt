package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.usecase.UseCase

interface GetCurrentTravelPlanUseCase: UseCase<GetCurrentTravelPlanUseCase.Input, GetCurrentTravelPlanUseCase.Output> {
    class Input(val employeeId: String)

    sealed class Output() {
        data class Success(val travelPlan: TravelPlan?) : Output()
        data class Failure(val message: String) : Output()
    }
}
