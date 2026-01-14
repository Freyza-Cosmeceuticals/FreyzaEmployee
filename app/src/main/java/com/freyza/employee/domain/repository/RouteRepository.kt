package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Route

interface RouteRepository {
    suspend fun getRoute(routeId: String): Result<Route>
}
