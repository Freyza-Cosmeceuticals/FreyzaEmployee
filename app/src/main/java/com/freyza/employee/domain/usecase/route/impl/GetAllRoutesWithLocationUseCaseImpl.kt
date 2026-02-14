package com.freyza.employee.domain.usecase.route.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllRoutesWithLocationUseCaseImpl(private val routeRepository: RouteRepository) :
  GetAllRoutesWithLocationUseCase {
  override suspend fun execute(input: GetAllRoutesWithLocationUseCase.Input): GetAllRoutesWithLocationUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = routeRepository.getAllRoutesWithLocation()) {
        is Result.Success -> GetAllRoutesWithLocationUseCase.Output.Success(result.data ?: listOf())
        is Result.Error -> GetAllRoutesWithLocationUseCase.Output.Failure(result.message ?: "")
        else -> GetAllRoutesWithLocationUseCase.Output.Failure(result.message ?: "")
      }
    }
  }
}
