package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.freyza.employee.domain.model.VisitType
import kotlin.time.Instant

@Entity(
  tableName = "pois",
  indices = [Index("locationId"), Index("type")]
)
data class PoiEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: VisitType,
  val locationId: String,
  val locationName: String? = null,
  val latitude: Double,
  val longitude: Double,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)
