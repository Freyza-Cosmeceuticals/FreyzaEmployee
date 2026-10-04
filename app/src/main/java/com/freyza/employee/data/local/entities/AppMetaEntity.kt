package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_meta")
data class AppMetaEntity(
  @PrimaryKey val id: Int = 1,
  val serverVersion: String? = null,
  val serverBuildNumber: Long = 0L,
  val lastVersionCheckAt: Long = 0L,
  val serverTimeOffset: Long = 0L,
  val lastTimeSyncAt: Long = 0L,
)
