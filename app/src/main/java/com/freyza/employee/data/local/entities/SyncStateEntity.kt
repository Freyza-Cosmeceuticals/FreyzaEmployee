package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
  IDLE, SYNCING, SUCCESS, FAILED
}

@Entity(tableName = "sync_state")
data class SyncStateEntity(
  @PrimaryKey val key: String,
  val lastAttemptAt: Long = 0L,
  val lastSuccessAt: Long = 0L,
  val status: SyncStatus = SyncStatus.IDLE,
  val errorMessage: String? = null,
)
