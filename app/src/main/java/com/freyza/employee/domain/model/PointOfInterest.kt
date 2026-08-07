package com.freyza.employee.domain.model

import kotlin.time.Instant

data class PointOfInterest(
  val id: String,

  val name: String,
  val type: VisitType,

  val locationId: String,

  val latitude: Double,
  val longitude: Double,

  val createdAt: Instant,
  val updatedAt: Instant?,
)
