package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.AppMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetaDao {
  @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
  fun getAppMetaFlow(): Flow<AppMetaEntity?>

  @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
  suspend fun getAppMeta(): AppMetaEntity?

  @Upsert
  suspend fun upsertAppMeta(appMeta: AppMetaEntity)
}
