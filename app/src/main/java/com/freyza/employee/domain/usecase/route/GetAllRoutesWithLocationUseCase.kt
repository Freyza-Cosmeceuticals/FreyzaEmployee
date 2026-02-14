package com.freyza.employee.domain.usecase.route

import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.usecase.UseCase

interface GetAllRoutesWithLocationUseCase :
  UseCase<GetAllRoutesWithLocationUseCase.Input, GetAllRoutesWithLocationUseCase.Output> {
  class Input()

  sealed class Output() {
    data class Success(val routes: List<RouteWithLocation>) : Output()
    data class Failure(val message: String) : Output()
  }
}
