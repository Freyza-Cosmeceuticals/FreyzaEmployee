package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
  suspend fun getRoute(routeId: String, forceRefresh: Boolean = false): Result<Route?>
  suspend fun getAllRoutes(forceRefresh: Boolean = false): Result<List<Route>>
  suspend fun getAllRoutesWithLocation(forceRefresh: Boolean = false): Result<List<RouteWithLocation>>
  fun observeAllRoutesWithLocation(): Flow<List<RouteWithLocation>>
  suspend fun getOrCreateRoute(srcLocId: String, destLocId: String): Result<Route>
  suspend fun refreshRoutes(): Result<Unit>
  fun clearCache()
}
