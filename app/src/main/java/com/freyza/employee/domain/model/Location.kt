package com.freyza.employee.domain.model

import kotlin.time.Instant

data class Location(
  val id: String,
  val name: String,
  val operational: Boolean,
  val createdAt: Instant,
  val updatedAt: Instant?,
)

fun dummyLocation(): Location = Location(
  id = "5754f651-ab79-4541-94f6-51ab790541f2",
  name = "Darbhanga",
  operational = true,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = null
)

fun dummyLocationAlt(): Location = Location(
  id = "ec81b4e5-c887-4968-81b4-e5c88759689d",
  name = "Bhagalpur",
  operational = true,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = null
)
