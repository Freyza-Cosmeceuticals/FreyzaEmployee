package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.RouteDto
import com.freyza.employee.data.network.dto.RouteWithLocationDto
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.toRoute
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

  private var routeCache = mutableMapOf<String, Route>()
  private var routesCache: List<Route>? = null
  private var cachedRoutesWithLocation: List<RouteWithLocation>? = null

  override suspend fun getRoute(routeId: String, forceRefresh: Boolean): Result<Route?> {
    if (!forceRefresh && routeCache.containsKey(routeId)) {
      Logger.d(TAG, "Cache hit for route ID: $routeId")
      return Result.Success(routeCache[routeId])
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying route with ID: $routeId from network")

        val routeDto = postgrest.from("route").select {
          filter {
            RouteDto::id eq routeId
          }
        }.decodeSingleOrNull<RouteDto>()

        val route = routeDto?.toDomain()
        if (route != null) {
          routeCache[routeId] = route
        }
        Result.Success(route)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getAllRoutes(forceRefresh: Boolean): Result<List<Route>> {
    if (!forceRefresh && routesCache != null) {
      Logger.d(TAG, "Cache hit for all routes")
      return Result.Success(routesCache!!)
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying all routes from network")

        val routesDto = postgrest.from("route").select().decodeList<RouteDto>()

        val routes = routesDto.map { it.toDomain() }
        routesCache = routes
        // Also populate individual cache
        routes.forEach { routeCache[it.id] = it }
        
        Result.Success(routes)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getAllRoutesWithLocation(forceRefresh: Boolean): Result<List<RouteWithLocation>> {
    if (!forceRefresh && cachedRoutesWithLocation != null) {
      Logger.d(TAG, "Cache hit for all routes with location")
      return Result.Success(cachedRoutesWithLocation!!)
    }

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying all routes with location from network")

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
        cachedRoutesWithLocation = routes
        
        // Also populate individual basic route cache
        routes.forEach { routeCache[it.id] = it.toRoute() }
        
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

        val route = routeDto.toDomain()
        
        // Invalidate caches
        routeCache.clear()
        routesCache = null
        cachedRoutesWithLocation = null
        
        Logger.d(TAG, "Caches invalidated after creating new route")

        Result.Success(route)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "RPC get_or_create_route failed: ${e.message}")
      Result.Error(e.message.toString())
    }
  }
}
