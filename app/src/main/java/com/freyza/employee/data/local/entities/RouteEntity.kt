package com.freyza.employee.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlin.time.Instant

@Entity(
  tableName = "routes",
  foreignKeys = [
    ForeignKey(
      entity = LocationEntity::class,
      parentColumns = ["id"],
      childColumns = ["srcLocId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = LocationEntity::class,
      parentColumns = ["id"],
      childColumns = ["destLocId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("srcLocId"), Index("destLocId")]
)
data class RouteEntity(
  @PrimaryKey val id: String,
  val srcLocId: String,
  val destLocId: String,
  val distanceKm: Float,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)

data class RouteWithLocationEntity(
  @Embedded val route: RouteEntity,
  @Relation(
    parentColumn = "srcLocId",
    entityColumn = "id"
  )
  val srcLoc: LocationEntity,
  @Relation(
    parentColumn = "destLocId",
    entityColumn = "id"
  )
  val destLoc: LocationEntity,
)
