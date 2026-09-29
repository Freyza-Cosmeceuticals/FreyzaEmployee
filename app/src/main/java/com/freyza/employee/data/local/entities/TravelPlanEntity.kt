package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.freyza.employee.domain.model.DayType
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Entity(
  tableName = "travel_plans",
  indices = [Index(value = ["employeeId", "month"], unique = true)]
)
data class TravelPlanEntity(
  @PrimaryKey val id: String,
  val employeeId: String,
  val month: LocalDate,
  val createdById: String,
  val salesTarget: Int = 15000,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)

@Entity(
  tableName = "travel_plan_entries",
  foreignKeys = [
    ForeignKey(
      entity = TravelPlanEntity::class,
      parentColumns = ["id"],
      childColumns = ["tpId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("tpId"), Index("date")]
)
data class TravelPlanEntryEntity(
  @PrimaryKey val id: String,
  val tpId: String,
  val date: LocalDate,
  val dayType: DayType,
  val routeId: String? = null,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)
