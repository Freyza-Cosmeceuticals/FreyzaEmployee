package com.freyza.employee.domain.usecase.route.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetRouteUseCaseImpl(val routeRepository: RouteRepository) : GetRouteUseCase {
    override suspend fun execute(input: GetRouteUseCase.Input): GetRouteUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = routeRepository.getRoute(input.id)) {
                is Result.Success -> GetRouteUseCase.Output.Success(result.data)
                is Result.Error -> GetRouteUseCase.Output.Failure(result.message ?: "")
                else -> GetRouteUseCase.Output.Failure(result.message ?: "")
            }
        }
    }
}
