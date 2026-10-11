package com.freyza.employee.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freyza.employee.domain.model.EmployeeTier
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Entity(tableName = "employees")
data class UserEntity(
  @PrimaryKey val id: String,
  val name: String,
  val email: String,
  val phone: String,
  val role: UserRole,
  val status: UserStatus,
  val tier: EmployeeTier? = null,
  val hqId: String? = null,
  val joiningDate: LocalDate,
  val resignDate: LocalDate? = null,
  val createdAt: Instant,
  val updatedAt: Instant? = null,
)
