package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.usecase.UseCase

interface GetTravelPlanEntriesUseCase :
    UseCase<GetTravelPlanEntriesUseCase.Input, GetTravelPlanEntriesUseCase.Output> {
    class Input(val tpId: String)

    sealed class Output() {
        data class Success(val travelPlanEntries: List<TravelPlanEntry>) : Output()
        data class Failure(val message: String) : Output()
    }
}
