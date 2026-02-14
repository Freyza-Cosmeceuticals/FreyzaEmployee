package com.freyza.employee.domain.model

import kotlin.time.Instant

data class Route(
  val id: String,

  val srcLocId: String,
  val destLocId: String,

  val distanceKm: Float,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

data class RouteWithLocation(
  val id: String,

  val srcLoc: Location,
  val destLoc: Location,

  val distanceKm: Float,

  val createdAt: Instant,
  val updatedAt: Instant?,

  )

fun RouteWithLocation.routeName(): String = "${srcLoc.name} -> ${destLoc.name}"

fun dummyRoute(): Route = Route(
  id = "3f6b851f-5c47-42b0-ab85-1f5c47c2b0d7",
  srcLocId = "9d119bf6-18ec-49e1-919b-f618eca9e10d",
  destLocId = "dc94c392-83b8-4a07-94c3-9283b89a07e0",
  distanceKm = 32.0f,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)

fun dummyRouteAlt(): Route = Route(
  id = "425e7094-6c13-410a-9e70-946c13310aa7",
  srcLocId = "e0bd2f8e-f09f-4c9b-bd2f-8ef09fcc9bab",
  destLocId = "c5aaf6a0-caf1-4830-aaf6-a0caf18830e2",
  distanceKm = 12.0f,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)
