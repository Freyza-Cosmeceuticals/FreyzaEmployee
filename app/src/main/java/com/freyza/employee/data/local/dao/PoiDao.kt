package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.PoiEntity
import com.freyza.employee.domain.model.VisitType
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
  @Query("SELECT * FROM pois WHERE locationId IN (:locationIds) ORDER BY name ASC")
  fun getPoisByLocationsFlow(locationIds: List<String>): Flow<List<PoiEntity>>

  @Query("SELECT * FROM pois WHERE locationId IN (:locationIds) AND type = :type ORDER BY name ASC")
  fun getPoisByLocationsAndTypeFlow(locationIds: List<String>, type: VisitType): Flow<List<PoiEntity>>

  @Query("SELECT COUNT(*) FROM pois")
  fun getPoiCountFlow(): Flow<Int>

  @Query("SELECT * FROM pois WHERE locationId IN (:locationIds) ORDER BY name ASC")
  suspend fun getPoisByLocations(locationIds: List<String>): List<PoiEntity>

  @Query("SELECT * FROM pois WHERE locationId IN (:locationIds) AND type = :type ORDER BY name ASC")
  suspend fun getPoisByLocationsAndType(locationIds: List<String>, type: VisitType): List<PoiEntity>

  @Query("SELECT * FROM pois WHERE id = :id LIMIT 1")
  suspend fun getPoiById(id: String): PoiEntity?

  @Upsert
  suspend fun upsertPois(pois: List<PoiEntity>)

  @Query("DELETE FROM pois")
  suspend fun clearAll()
}
