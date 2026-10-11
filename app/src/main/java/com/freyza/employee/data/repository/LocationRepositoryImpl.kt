package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.data.local.dao.LocationDao
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toEntity
import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.repository.LocationRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocationRepositoryImpl(
  private val postgrest: Postgrest,
  private val locationDao: LocationDao,
) : LocationRepository {

  companion object {
    const val TAG: String = "LocationRepo"
  }

  override fun clearCache() {
    throw NotImplementedError()
  }

  override fun observeAllLocations(): Flow<List<Location>> {
    return locationDao.getAllLocationsFlow().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun getLocation(locationId: String, forceRefresh: Boolean): Result<Location?> {
    val cached = locationDao.getLocationById(locationId)
    return if (cached != null && !forceRefresh) {
      Result.Success(cached.toDomain())
    } else {
      refreshLocations()
      Result.Success(locationDao.getLocationById(locationId)?.toDomain())
    }
  }

  override suspend fun getAllLocations(forceRefresh: Boolean): Result<List<Location>> {
    val cached = locationDao.getAllLocations()
    return if (cached.isNotEmpty() && !forceRefresh) {
      Result.Success(cached.map { it.toDomain() })
    } else {
      refreshLocations()
      Result.Success(locationDao.getAllLocations().map { it.toDomain() })
    }
  }

  override suspend fun refreshLocations(): Result<Unit> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        val locationDtos = postgrest.from("location").select().decodeList<LocationDto>()
        val entities = locationDtos.map { it.toDomain().toEntity() }
        locationDao.upsertLocations(entities)
      }
    }
  }
}
