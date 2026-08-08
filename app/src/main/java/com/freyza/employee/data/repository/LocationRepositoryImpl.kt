package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.repository.LocationRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepositoryImpl(private val postgrest: Postgrest) : LocationRepository {
  companion object {
    const val TAG: String = "LocationRepo"
  }

  // Cache for the complete list of locations
  private var cachedLocations: List<Location>? = null

  // Cache for location by id
  private var cachedLocationsById: MutableMap<String, Location?> = mutableMapOf()

  override suspend fun getLocation(locationId: String, forceRefresh: Boolean): Result<Location?> {
    if (!forceRefresh && cachedLocationsById[locationId] != null) {
      Logger.d(TAG, "Returning location from cache")
      return Result.Success(cachedLocationsById[locationId])
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying location with ID: $locationId from network")

        val locationDto = postgrest.from("location").select {
          filter {
            LocationDto::id eq locationId
          }
        }.decodeSingleOrNull<LocationDto>()

        val location = locationDto?.toDomain()
        if (location != null) {
          cachedLocationsById[locationId] = location
        }

        location
      }
    }
  }

  override suspend fun getAllLocations(forceRefresh: Boolean): Result<List<Location>> {
    if (!forceRefresh && cachedLocations != null) {
      Logger.d(TAG, "Returning locations from cache")
      return Result.Success(cachedLocations!!)
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying all locations from network")

        val locationDtos = postgrest.from("location").select().decodeList<LocationDto>()
        val locations = locationDtos.map { it.toDomain() }

        cachedLocations = locations
        cachedLocations?.forEach { cachedLocationsById[it.id] = it }

        locations
      }
    }
  }
}
