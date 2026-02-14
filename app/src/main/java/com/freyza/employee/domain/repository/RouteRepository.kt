package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.RouteWithLocation

interface RouteRepository {
  suspend fun getRoute(routeId: String): Result<Route>
  suspend fun getAllRoutes(): Result<List<Route>>
  suspend fun getAllRoutesWithLocation(): Result<List<RouteWithLocation>>
}
