package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.RouteEntity
import com.freyza.employee.data.local.entities.RouteWithLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
  @Transaction
  @Query("SELECT * FROM routes")
  fun getAllRoutesWithLocationFlow(): Flow<List<RouteWithLocationEntity>>

  @Query("SELECT COUNT(*) FROM routes")
  fun getRouteCountFlow(): Flow<Int>

  @Transaction
  @Query("SELECT * FROM routes")
  suspend fun getAllRoutesWithLocation(): List<RouteWithLocationEntity>

  @Transaction
  @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
  suspend fun getRouteWithLocationById(id: String): RouteWithLocationEntity?

  @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
  suspend fun getRouteById(id: String): RouteEntity?

  @Query("SELECT * FROM routes")
  suspend fun getAllRoutes(): List<RouteEntity>

  @Upsert
  suspend fun upsertRoutes(routes: List<RouteEntity>)

  @Upsert
  suspend fun upsertRoute(route: RouteEntity)

  @Query("DELETE FROM routes")
  suspend fun clearAll()
}
