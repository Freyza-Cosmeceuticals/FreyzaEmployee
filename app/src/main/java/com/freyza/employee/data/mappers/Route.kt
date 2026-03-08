package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.RouteDto
import com.freyza.employee.data.network.dto.RouteWithLocationDto
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import kotlin.time.Instant

fun RouteDto.toDomain(): Route {
  return Route(
    id = id,
    srcLocId = srcLocId,
    destLocId = destLocId,
    distanceKm = distanceKm,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
  )
}

fun RouteWithLocationDto.toDomain(): RouteWithLocation {
  return RouteWithLocation(
    id = id,
    srcLoc = Location(
      id = srcLoc.id,
      name = srcLoc.name,
      operational = srcLoc.operational,
      createdAt = Instant.parse(srcLoc.createdAt),
      updatedAt = srcLoc.updatedAt?.let { Instant.parse(it) }
    ),
    destLoc = Location(
      id = destLoc.id,
      name = destLoc.name,
      operational = destLoc.operational,
      createdAt = Instant.parse(destLoc.createdAt),
      updatedAt = destLoc.updatedAt?.let { Instant.parse(it) }
    ),
    distanceKm = distanceKm,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) },
  )
}
