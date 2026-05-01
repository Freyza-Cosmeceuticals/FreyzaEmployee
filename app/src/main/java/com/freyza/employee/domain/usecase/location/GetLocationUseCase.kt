package com.freyza.employee.domain.usecase.location

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.repository.LocationRepository

class GetLocationUseCase(private val locationRepository: LocationRepository) {
  suspend operator fun invoke(id: String): Result<Location?> {
    return locationRepository.getLocation(id)
  }
}
