package com.freyza.employee.domain.usecase.location.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetLocationUseCaseImpl(val locationRepository: LocationRepository) : GetLocationUseCase {
    override suspend fun execute(input: GetLocationUseCase.Input): GetLocationUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = locationRepository.getLocation(input.id)) {
                is Result.Success -> GetLocationUseCase.Output.Success(result.data)
                is Result.Error -> GetLocationUseCase.Output.Failure(result.message ?: "")
                else -> GetLocationUseCase.Output.Failure(result.message ?: "")
            }
        }
    }
}
