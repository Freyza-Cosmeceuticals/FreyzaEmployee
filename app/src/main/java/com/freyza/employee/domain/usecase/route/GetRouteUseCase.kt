package com.freyza.employee.domain.usecase.route

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.repository.RouteRepository

class GetRouteUseCase(private val routeRepository: RouteRepository) {
  suspend operator fun invoke(id: String): Result<Route?> {
    return routeRepository.getRoute(id)
  }
}
