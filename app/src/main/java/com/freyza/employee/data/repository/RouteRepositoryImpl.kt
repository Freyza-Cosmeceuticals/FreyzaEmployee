package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.RouteDto
import com.freyza.employee.data.network.dto.RouteWithLocationDto
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.repository.RouteRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RouteRepositoryImpl(private val postgrest: Postgrest) : RouteRepository {
  companion object {
    const val TAG: String = "RouteRepository"
  }

  override suspend fun getRoute(routeId: String): Result<Route?> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying route with ID: $routeId")

        val routeDto = postgrest.from("route").select {
          filter {
            RouteDto::id eq routeId
          }
        }.decodeSingleOrNull<RouteDto>()

        Result.Success(routeDto?.toDomain())
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

        val routes = routesDto.map { it.toDomain() }
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
         """.trimIndent()
          )
        ).decodeList<RouteWithLocationDto>()

        val routes = routesDto.map { it.toDomain() }
        Result.Success(routes)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getOrCreateRoute(srcLocId: String, destLocId: String): Result<Route> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Calling RPC get_or_create_route for $srcLocId -> $destLocId")

        val routeDto = postgrest.rpc(
          function = "get_or_create_route",
          parameters = mapOf(
            "p_src_loc_id" to srcLocId,
            "p_dest_loc_id" to destLocId
          )
        ).decodeAs<RouteDto>()

        Result.Success(routeDto.toDomain())
      }
    } catch (e: Exception) {
      Logger.e(TAG, "RPC get_or_create_route failed: ${e.message}")
      Result.Error(e.message.toString())
    }
  }
}
