package com.freyza.employee.data.mappers

import com.freyza.employee.data.local.entities.LocationEntity
import com.freyza.employee.data.local.entities.PoiEntity
import com.freyza.employee.data.local.entities.RouteEntity
import com.freyza.employee.data.local.entities.RouteWithLocationEntity
import com.freyza.employee.data.local.entities.TravelPlanEntity
import com.freyza.employee.data.local.entities.TravelPlanEntryEntity
import com.freyza.employee.data.local.entities.UserEntity
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.User

// Location Mappers
fun LocationEntity.toDomain(): Location = Location(
  id = id,
  name = name,
  operational = operational,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Location.toEntity(): LocationEntity = LocationEntity(
  id = id,
  name = name,
  operational = operational,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

// Route Mappers
fun RouteEntity.toDomain(): Route = Route(
  id = id,
  srcLocId = srcLocId,
  destLocId = destLocId,
  distanceKm = distanceKm,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Route.toEntity(): RouteEntity = RouteEntity(
  id = id,
  srcLocId = srcLocId,
  destLocId = destLocId,
  distanceKm = distanceKm,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun RouteWithLocationEntity.toDomain(): RouteWithLocation = RouteWithLocation(
  id = route.id,
  srcLoc = srcLoc.toDomain(),
  destLoc = destLoc.toDomain(),
  distanceKm = route.distanceKm,
  createdAt = route.createdAt,
  updatedAt = route.updatedAt,
)

// User Mappers
fun UserEntity.toDomain(): User = User(
  id = id,
  name = name,
  email = email,
  phone = phone,
  role = role,
  status = status,
  tier = tier,
  hqId = hqId,
  joiningDate = joiningDate,
  resignDate = resignDate,
  createdAt = createdAt,
  updatedAt = updatedAt,
  userInfo = null,
)

fun User.toEntity(): UserEntity = UserEntity(
  id = id,
  name = name,
  email = email,
  phone = phone,
  role = role,
  status = status,
  tier = tier,
  hqId = hqId,
  joiningDate = joiningDate,
  resignDate = resignDate,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

// Travel Plan Mappers
fun TravelPlanEntity.toDomain(entries: List<TravelPlanEntry> = emptyList()): TravelPlan = TravelPlan(
  id = id,
  employeeId = employeeId,
  month = month,
  createdById = createdById,
  travelPlanEntries = entries,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun TravelPlan.toEntity(): TravelPlanEntity = TravelPlanEntity(
  id = id,
  employeeId = employeeId,
  month = month,
  createdById = createdById,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun TravelPlanEntryEntity.toDomain(): TravelPlanEntry = TravelPlanEntry(
  id = id,
  tpId = tpId,
  date = date,
  dayType = dayType,
  routeId = routeId,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun TravelPlanEntry.toEntity(): TravelPlanEntryEntity = TravelPlanEntryEntity(
  id = id,
  tpId = tpId,
  date = date,
  dayType = dayType,
  routeId = routeId,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

// POI Mappers
fun PoiEntity.toDomain(): PointOfInterest = PointOfInterest(
  id = id,
  name = name,
  type = type,
  locationId = locationId,
  locationName = locationName,
  latitude = latitude,
  longitude = longitude,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun PointOfInterest.toEntity(): PoiEntity = PoiEntity(
  id = id,
  name = name,
  type = type,
  locationId = locationId,
  locationName = locationName,
  latitude = latitude,
  longitude = longitude,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
