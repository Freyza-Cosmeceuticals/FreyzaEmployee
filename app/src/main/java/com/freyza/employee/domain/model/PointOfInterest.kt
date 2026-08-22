package com.freyza.employee.domain.model

import kotlin.time.Instant

data class PointOfInterest(
  val id: String,

  val name: String,
  val type: VisitType,

  val locationId: String,
  val locationName: String? = null,

  val latitude: Double,
  val longitude: Double,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

fun dummyPoiDoctor(): PointOfInterest = PointOfInterest(
  id = "8f8a31e5-d5b4-453e-8a31-e5d5b4d53e4b",
  name = "Dr. Mario Mario",
  type = VisitType.DOCTOR,
  locationId = "loc-1",
  locationName = "Mushroom Kingdom",
  latitude = 34.632,
  longitude = 55.246,
  createdAt = Instant.parse("2025-10-10T18:15:03.410Z"),
  updatedAt = null
)

fun dummyPoiStockist(): PointOfInterest = PointOfInterest(
  id = "9e54e0bd-0bb7-4a49-94e0-bd0bb72a49cc",
  name = "Peach's Supplies",
  type = VisitType.STOCKIST,
  locationId = "loc-1",
  locationName = "Mushroom Kingdom",
  latitude = 45.367,
  longitude = 43.326,
  createdAt = Instant.parse("2025-10-11T10:15:03.410Z"),
  updatedAt = null
)

fun dummyPoiChemist(): PointOfInterest = PointOfInterest(
  id = "91367409-f38a-4023-b674-09f38a80236f",
  name = "Toad's Pharmacy",
  type = VisitType.CHEMIST,
  locationId = "loc-2",
  locationName = "Bowser's Castle",
  latitude = 45.653,
  longitude = 22.216,
  createdAt = Instant.parse("2025-10-12T12:15:03.410Z"),
  updatedAt = null
)

fun dummyPois(): List<PointOfInterest> = listOf(
  dummyPoiDoctor(),
  dummyPoiStockist(),
  dummyPoiChemist()
)
