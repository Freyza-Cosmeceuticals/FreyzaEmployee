package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.data.network.dto.PoiLocationDto
import com.freyza.employee.data.network.dto.PointOfInterestDto
import com.freyza.employee.domain.model.PointOfInterest
import kotlin.time.Instant

fun PointOfInterestDto.toDomain(): PointOfInterest = PointOfInterest(
  id = id,
  name = name,
  type = type,
  locationId = locationId,
  locationName = location?.name,
  latitude = latitude,
  longitude = longitude,
  createdAt = Instant.parse(createdAt),
  updatedAt = updatedAt?.let { Instant.parse(it) }
)

fun PointOfInterest.toDto(): PointOfInterestDto = PointOfInterestDto(
  id = id,
  name = name,
  type = type,
  locationId = locationId,
  location = locationName?.let { PoiLocationDto(locationId, it) },
  latitude = latitude,
  longitude = longitude,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt?.toString()
)
