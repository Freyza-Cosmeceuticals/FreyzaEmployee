package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.TravelPlanDto
import com.freyza.employee.data.network.dto.TravelPlanEntryDto
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

fun TravelPlanDto.toDomain(): TravelPlan {
  return TravelPlan(
    id = id,
    employeeId = employeeId,
    month = LocalDate.parse(month),
    createdById = createdById,
    travelPlanEntries = emptyList(),
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
  )
}

fun TravelPlanEntryDto.toDomain(): TravelPlanEntry {
  return TravelPlanEntry(
    id = id,
    tpId = tpId,
    date = LocalDate.parse(date),
    dayType = dayType,
    routeId = routeId,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
  )
}

fun TravelPlan.toDto(): TravelPlanDto{
  return TravelPlanDto(
    id = id,
    employeeId = employeeId,
    month = month.toString(),
    createdById = createdById,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}
