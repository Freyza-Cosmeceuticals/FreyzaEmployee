package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Location

interface LocationRepository {
    suspend fun getLocation(locationId: String): Result<Location>
}
