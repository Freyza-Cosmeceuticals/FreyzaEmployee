package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.local.dao.LocationDao
import com.freyza.employee.data.local.dao.RouteDao
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RouteRepositoryImpl(
  private val postgrest: Postgrest,
  private val routeDao: RouteDao,
  private val locationDao: LocationDao,
) : RouteRepository {

  companion object {
    const val TAG: String = "RouteRepository"
  }

  override fun clearCache() {
    throw NotImplementedError()
  }

  override fun observeAllRoutesWithLocation(): Flow<List<RouteWithLocation>> {
    return routeDao.getAllRoutesWithLocationFlow().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun getRoute(routeId: String, forceRefresh: Boolean): Result<Route?> {
    val cached = routeDao.getRouteById(routeId)
    return if (cached != null && !forceRefresh) {
      Result.Success(cached.toDomain())
    } else {
      refreshRoutes()
      Result.Success(routeDao.getRouteById(routeId)?.toDomain())
    }
  }

  override suspend fun getAllRoutes(forceRefresh: Boolean): Result<List<Route>> {
    val cached = routeDao.getAllRoutes()
    return if (cached.isNotEmpty() && !forceRefresh) {
      Result.Success(cached.map { it.toDomain() })
    } else {
      refreshRoutes()
      Result.Success(routeDao.getAllRoutes().map { it.toDomain() })
    }
  }

  override suspend fun getAllRoutesWithLocation(forceRefresh: Boolean): Result<List<RouteWithLocation>> {
    val cached = routeDao.getAllRoutesWithLocation()
    return if (cached.isNotEmpty() && !forceRefresh) {
      Result.Success(cached.map { it.toDomain() })
    } else {
      refreshRoutes()
      Result.Success(routeDao.getAllRoutesWithLocation().map { it.toDomain() })
    }
  }

  override suspend fun getOrCreateRoute(srcLocId: String, destLocId: String): Result<Route> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Calling RPC get_or_create_route for $srcLocId -> $destLocId")

        val routeDto = postgrest.rpc(
          function = "get_or_create_route", parameters = mapOf(
            "p_src_loc_id" to srcLocId, "p_dest_loc_id" to destLocId
          )
        ).decodeAs<RouteDto>()

        val route = routeDto.toDomain()
        routeDao.upsertRoute(route.toEntity())
        route
      }
    }
  }

  override suspend fun refreshRoutes(): Result<Unit> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        val routesDto = postgrest.from("route").select(
          Columns.raw(
            """
          *,
          srcLoc:location!route_srcLocId_fkey(*),
          destLoc:location!route_destLocId_fkey(*)
         """.trimIndent()
          )
        ).decodeList<RouteWithLocationDto>()

        val routeEntities = routesDto.map { it.toDomain().toRoute().toEntity() }
        val locationEntities = routesDto.flatMap { dto ->
          listOf(dto.srcLoc.toDomain().toEntity(), dto.destLoc.toDomain().toEntity())
        }.distinctBy { it.id }

        locationDao.upsertLocations(locationEntities)
        routeDao.upsertRoutes(routeEntities)
      }
    }
  }
}
