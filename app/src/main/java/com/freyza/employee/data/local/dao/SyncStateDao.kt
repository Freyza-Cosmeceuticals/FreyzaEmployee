package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.SyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {
  @Query("SELECT * FROM sync_state")
  fun getAllSyncStatesFlow(): Flow<List<SyncStateEntity>>

  @Query("SELECT * FROM sync_state WHERE `key` = :key LIMIT 1")
  suspend fun getSyncState(key: String): SyncStateEntity?

  @Query("SELECT * FROM sync_state WHERE `key` = :key LIMIT 1")
  fun getSyncStateFlow(key: String): Flow<SyncStateEntity?>

  @Upsert
  suspend fun upsertSyncState(syncState: SyncStateEntity)
}
