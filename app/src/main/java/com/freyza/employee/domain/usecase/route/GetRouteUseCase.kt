package com.freyza.employee.domain.usecase.route

import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.usecase.UseCase

interface GetRouteUseCase : UseCase<GetRouteUseCase.Input, GetRouteUseCase.Output> {
  class Input(val id: String)

  sealed class Output() {
    data class Success(val route: Route?) : Output()
    data class Failure(val message: String) : Output()
  }
}
