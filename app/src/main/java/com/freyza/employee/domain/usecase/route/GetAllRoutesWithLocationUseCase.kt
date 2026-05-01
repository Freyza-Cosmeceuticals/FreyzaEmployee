package com.freyza.employee.domain.usecase.route

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.repository.RouteRepository

class GetAllRoutesWithLocationUseCase(private val routeRepository: RouteRepository) {
  suspend operator fun invoke(): Result<List<RouteWithLocation>> {
    return when (val result = routeRepository.getAllRoutesWithLocation()) {
      is Result.Success -> Result.Success(result.data ?: listOf())
      else -> result
    }
  }
}
