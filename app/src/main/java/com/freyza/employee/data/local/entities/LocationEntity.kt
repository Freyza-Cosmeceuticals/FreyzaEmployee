package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "locations")
data class LocationEntity(
  @PrimaryKey val id: String,
  val name: String,
  val operational: Boolean = true,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)
