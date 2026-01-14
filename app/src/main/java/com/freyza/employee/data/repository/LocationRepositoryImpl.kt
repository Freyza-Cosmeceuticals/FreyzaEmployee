package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.repository.LocationRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class LocationRepositoryImpl(private val postgrest: Postgrest) : LocationRepository {
    companion object {
        const val TAG: String = "LOCATION_REPO"
    }

    override suspend fun getLocation(locationId: String): Result<Location> {
        return try {
            withContext(Dispatchers.IO) {
                Logger.d(TAG, "Querying location with ID: $locationId")

                val locationDto = postgrest.from("location").select() {
                    filter {
                        LocationDto::id eq locationId
                    }
                }.decodeSingleOrNull<LocationDto>()

                val location = locationDto?.let {
                    Location(
                        id = it.id,
                        name = it.name,
                        operational = it.operational,
                        createdAt = Instant.parse(it.createdAt),
                        updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
                    )
                }

                Result.Success(location)
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}
