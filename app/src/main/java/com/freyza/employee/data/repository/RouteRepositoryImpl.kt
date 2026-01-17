package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.RouteDto
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.repository.RouteRepository
import io.github.jan.supabase.postgrest.Postgrest
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
}
