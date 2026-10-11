package com.freyza.employee.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.freyza.employee.data.local.dao.AppMetaDao
import com.freyza.employee.data.local.dao.LocationDao
import com.freyza.employee.data.local.dao.PoiDao
import com.freyza.employee.data.local.dao.RouteDao
import com.freyza.employee.data.local.dao.SyncStateDao
import com.freyza.employee.data.local.dao.TravelPlanDao
import com.freyza.employee.data.local.dao.UserDao
import com.freyza.employee.data.local.entities.AppMetaEntity
import com.freyza.employee.data.local.entities.LocationEntity
import com.freyza.employee.data.local.entities.PoiEntity
import com.freyza.employee.data.local.entities.RouteEntity
import com.freyza.employee.data.local.entities.SyncStateEntity
import com.freyza.employee.data.local.entities.TravelPlanEntity
import com.freyza.employee.data.local.entities.TravelPlanEntryEntity
import com.freyza.employee.data.local.entities.UserEntity

@Database(
  entities = [
    AppMetaEntity::class,
    SyncStateEntity::class,
    LocationEntity::class,
    RouteEntity::class,
    UserEntity::class,
    TravelPlanEntity::class,
    TravelPlanEntryEntity::class,
    PoiEntity::class,
  ],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FreyzaDatabase : RoomDatabase() {
  abstract fun appMetaDao(): AppMetaDao
  abstract fun syncStateDao(): SyncStateDao
  abstract fun locationDao(): LocationDao
  abstract fun routeDao(): RouteDao
  abstract fun userDao(): UserDao
  abstract fun travelPlanDao(): TravelPlanDao
  abstract fun poiDao(): PoiDao
}
