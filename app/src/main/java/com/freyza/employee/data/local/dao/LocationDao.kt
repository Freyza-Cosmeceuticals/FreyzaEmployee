package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
  @Query("SELECT * FROM locations ORDER BY name ASC")
  fun getAllLocationsFlow(): Flow<List<LocationEntity>>

  @Query("SELECT COUNT(*) FROM locations")
  fun getLocationCountFlow(): Flow<Int>

  @Query("SELECT * FROM locations ORDER BY name ASC")
  suspend fun getAllLocations(): List<LocationEntity>

  @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
  suspend fun getLocationById(id: String): LocationEntity?

  @Upsert
  suspend fun upsertLocations(locations: List<LocationEntity>)

  @Upsert
  suspend fun upsertLocation(location: LocationEntity)

  @Query("DELETE FROM locations")
  suspend fun clearAll()
}
