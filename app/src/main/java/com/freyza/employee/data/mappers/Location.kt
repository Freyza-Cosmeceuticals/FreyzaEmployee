package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.domain.model.Location
import kotlin.time.Instant

fun LocationDto.toDomain(): Location {
  return Location(
    id = id,
    name = name,
    operational = operational,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
  )
}
