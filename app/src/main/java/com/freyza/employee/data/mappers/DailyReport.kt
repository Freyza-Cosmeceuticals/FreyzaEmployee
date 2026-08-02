package com.freyza.employee.data.mappers

import com.freyza.employee.core.util.Money
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
    travellingWithId = travellingWithId,
    ta = ta?.let { Money(it) },
    da = da?.let { Money(it) },
    totalExpense = totalExpense?.let { Money(it) },
    visits = visits?.map { it.toDomain() } ?: emptyList(),
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
    travellingWithId = travellingWithId,
    ta = ta?.amount,
    da = da?.amount,
    totalExpense = totalExpense?.amount,
    locked = locked,
    lockedAt = lockedAt?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}
