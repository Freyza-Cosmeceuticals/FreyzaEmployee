package com.freyza.employee.data.repository

import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.TravelPlanDto
import com.freyza.employee.data.network.dto.TravelPlanEntryDto
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.repository.TravelPlanRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

class TravelPlanRepositoryImpl(private val postgrest: Postgrest) : TravelPlanRepository {

    companion object {
        const val TAG: String = "TRAVEL_PLAN_REPO"
    }

    override suspend fun getCurrentTravelPlan(
        employeeId: String,
        withEntries: Boolean
    ): Result<TravelPlan> {
        return try {
            val today = Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE))
            val thisMonth = DateFormatter.format(
                LocalDate(year = today.year, month = today.month, day = 1),
                DateFormatter.FormattingType.MACHINE
            )

            withContext(Dispatchers.IO) {
                Logger.d(TAG, "Querying travelPlan for current employee and month: $thisMonth")

                val travelPlanDto = postgrest.from("travelPlan").select {
                    filter {
                        TravelPlanDto::employeeId eq employeeId
                        TravelPlanDto::month eq thisMonth
                    }
                }.decodeSingleOrNull<TravelPlanDto>()

                val travelPlan = travelPlanDto?.let {
                    TravelPlan(
                        id = it.id,
                        employeeId = it.employeeId,
                        month = LocalDate.parse(it.month),
                        createdById = it.createdById,
                        travelPlanEntries = listOf(),
                        createdAt = Instant.parse(it.createdAt),
                        updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
                    )
                }

                Result.Success(travelPlan)
            }

        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getTodayTravelPlanEntry(tpId: String): Result<TravelPlanEntry> {
        return try {
            val today = Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE)).plus(5, DateTimeUnit.DateBased.DayBased(1))
            val thisDay = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

            withContext(Dispatchers.IO) {
                Logger.d(TAG, "Querying travelPlanEntry for current employee and day: $thisDay")

                val travelPlanEntryDto = postgrest.from("travelPlanEntry").select {
                    filter {
                        TravelPlanEntryDto::tpId eq tpId
                        TravelPlanEntryDto::date eq thisDay
                    }
                }.decodeSingleOrNull<TravelPlanEntryDto>()

                val travelPlanEntry = travelPlanEntryDto?.let {
                    TravelPlanEntry(
                        id = it.id,
                        tpId = it.tpId,
                        date = LocalDate.parse(it.date),
                        dayType = it.dayType,
                        routeId = it.routeId,
                        createdAt = Instant.parse(it.createdAt),
                        updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
                    )
                }

                Result.Success(travelPlanEntry)
            }

        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getTravelPlan(id: String): Result<TravelPlan> {
        return try {
            withContext(Dispatchers.IO) {
                val travelPlanDto = postgrest.from("travelPlan").select {
                    filter { TravelPlanDto::id eq id }
                }.decodeSingleOrNull<TravelPlanDto>()

                val travelPlan = travelPlanDto?.let {
                    TravelPlan(
                        id = it.id,
                        employeeId = it.employeeId,
                        month = LocalDate.parse(it.month),
                        createdById = it.createdById,
                        travelPlanEntries = listOf(),
                        createdAt = Instant.parse(it.createdAt),
                        updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
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
