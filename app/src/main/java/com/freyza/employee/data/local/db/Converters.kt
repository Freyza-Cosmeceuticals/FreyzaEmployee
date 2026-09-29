package com.freyza.employee.data.local.db

import androidx.room.TypeConverter
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.EmployeeTier
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import com.freyza.employee.domain.model.VisitType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

class Converters {

  @TypeConverter
  fun fromLocalDate(value: LocalDate?): String? = value?.toString()

  @TypeConverter
  fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

  @TypeConverter
  fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

  @TypeConverter
  fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

  @TypeConverter
  fun fromInstant(value: Instant?): String? = value?.toString()

  @TypeConverter
  fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

  @TypeConverter
  fun fromUserRole(value: UserRole?): String? = value?.name

  @TypeConverter
  fun toUserRole(value: String?): UserRole? = value?.let { UserRole.valueOf(it) }

  @TypeConverter
  fun fromUserStatus(value: UserStatus?): String? = value?.name

  @TypeConverter
  fun toUserStatus(value: String?): UserStatus? = value?.let { UserStatus.valueOf(it) }

  @TypeConverter
  fun fromEmployeeTier(value: EmployeeTier?): String? = value?.name

  @TypeConverter
  fun toEmployeeTier(value: String?): EmployeeTier? = value?.let { EmployeeTier.valueOf(it) }

  @TypeConverter
  fun fromDayType(value: DayType?): String? = value?.name

  @TypeConverter
  fun toDayType(value: String?): DayType? = value?.let { DayType.valueOf(it) }

  @TypeConverter
  fun fromVisitType(value: VisitType?): String? = value?.name

  @TypeConverter
  fun toVisitType(value: String?): VisitType? = value?.let { VisitType.valueOf(it) }
}
