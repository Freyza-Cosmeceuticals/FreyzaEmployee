package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.DailyReportDto
import com.freyza.employee.domain.model.DailyReport
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

fun DailyReportDto.toDomain(): DailyReport {
  return DailyReport(
    id = id,
    employeeId = employeeId,
    date = LocalDate.parse(date),
    dayType = dayType,
    routeId = routeId,
    ta = ta,
    da = da,
    totalExpense = totalExpense,
    visits = emptyList(),
    locked = locked,
    lockedAt = lockedAt?.let { Instant.parse(it) },
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
  )
}

fun DailyReport.toDto(): DailyReportDto {
  return DailyReportDto(
    id = id,
    employeeId = employeeId,
    date = date.toString(),
    dayType = dayType,
    routeId = routeId,
    ta = ta,
    da = da,
    totalExpense = totalExpense,
    locked = locked,
    lockedAt = lockedAt?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}
