package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.TravelPlanDto
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.repository.TravelPlanRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TravelPlanRepositoryImpl(private val postgrest: Postgrest) : TravelPlanRepository {

    companion object {
        const val TAG: String = "TRAVEL_PLAN_REPO"
    }

    override suspend fun getCurrentTravelPlan(employeeId: String): Result<TravelPlan> {
        return try {
            withContext(Dispatchers.IO) {
                val travelPlanDto = postgrest.from("travelPlan").select() {
                    filter { TravelPlanDto::employeeId eq employeeId }
                }.decodeList<TravelPlanDto>().firstOrNull()

                val travelPlan = travelPlanDto?.let {
                    TravelPlan(
                        id = it.id,
                        employeeId = it.employeeId,
                        month = it.month,
                        createdById = it.createdById,
                        travelPlanEntries = listOf(),
                        createdAt = it.createdById,
                        updatedAt = it.updatedAt
                    )
                }

                Result.Success(travelPlan)
            }

        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getTravelPlan(id: String): Result<TravelPlan> {
        return try {
            withContext(Dispatchers.IO) {
                val travelPlanDto = postgrest.from("travelPlan").select() {
                    filter { TravelPlanDto::id eq id }
                }.decodeSingleOrNull<TravelPlanDto>()

                val travelPlan = travelPlanDto?.let {
                    TravelPlan(
                        id = it.id,
                        employeeId = it.employeeId,
                        month = it.month,
                        createdById = it.createdById,
                        travelPlanEntries = listOf(),
                        createdAt = it.createdById,
                        updatedAt = it.updatedAt
                    )
                }

                Result.Success(travelPlan)
            }

        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}
