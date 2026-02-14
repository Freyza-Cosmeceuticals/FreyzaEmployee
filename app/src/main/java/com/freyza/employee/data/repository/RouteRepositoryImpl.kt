package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.RouteDto
import com.freyza.employee.data.network.dto.RouteWithLocationDto
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.repository.RouteRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class RouteRepositoryImpl(private val postgrest: Postgrest) : RouteRepository {
  companion object {
    const val TAG: String = "ROUTE_REPO"
  }

  override suspend fun getRoute(routeId: String): Result<Route> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying route with ID: $routeId")

        val routeDto = postgrest.from("route").select {
          filter {
            RouteDto::id eq routeId
          }
        }.decodeSingleOrNull<RouteDto>()

        val route = routeDto?.let {
          Route(
            id = it.id,
            srcLocId = it.srcLocId,
            destLocId = it.destLocId,
            distanceKm = it.distanceKm,
            createdAt = Instant.parse(it.createdAt),
            updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
          )
        }

        Result.Success(route)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getAllRoutes(): Result<List<Route>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying all routes")

        val routesDto = postgrest.from("route").select().decodeList<RouteDto>()

        val routes = routesDto.map {
          Route(
            id = it.id,
            srcLocId = it.srcLocId,
            destLocId = it.destLocId,
            distanceKm = it.distanceKm,
            createdAt = Instant.parse(it.createdAt),
            updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
          )
        }

        Result.Success(routes)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getAllRoutesWithLocation(): Result<List<RouteWithLocation>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying all routes with location")

        val routesDto = postgrest.from("route").select(
          Columns.raw(
            """
          *,
          srcLoc:location!route_srcLocId_fkey(*),
          destLoc:location!route_destLocId_fkey(*)
         """
          )
        ).decodeList<RouteWithLocationDto>()

        val routes = routesDto.map {
          RouteWithLocation(
            id = it.id,
            srcLoc = Location(
              id = it.srcLoc.id,
              name = it.srcLoc.name,
              operational = it.srcLoc.operational,
              createdAt = Instant.parse(it.srcLoc.createdAt),
              updatedAt = it.srcLoc.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) }
            ),
            destLoc = Location(
              id = it.destLoc.id,
              name = it.destLoc.name,
              operational = it.destLoc.operational,
              createdAt = Instant.parse(it.destLoc.createdAt),
              updatedAt = it.destLoc.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) }
            ),
            distanceKm = it.distanceKm,
            createdAt = Instant.parse(it.createdAt),
            updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
          )
        }

        Result.Success(routes)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
