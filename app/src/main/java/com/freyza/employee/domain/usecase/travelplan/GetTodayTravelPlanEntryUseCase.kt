package com.freyza.employee.domain.usecase.travelplan

import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.usecase.UseCase

interface GetTodayTravelPlanEntryUseCase :
  UseCase<GetTodayTravelPlanEntryUseCase.Input, GetTodayTravelPlanEntryUseCase.Output> {
  class Input(val tpId: String)

  sealed class Output() {
    data class Success(val travelPlanEntry: TravelPlanEntry?) : Output()
    data class Failure(val message: String) : Output()
  }
}
