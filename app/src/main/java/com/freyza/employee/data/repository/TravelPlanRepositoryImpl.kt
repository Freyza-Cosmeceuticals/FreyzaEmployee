package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.data.mappers.toDomain
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
import kotlinx.datetime.plus

class TravelPlanRepositoryImpl(
  private val postgrest: Postgrest,
  private val serverTime: ServerTime,
) : TravelPlanRepository {

  companion object {
    const val TAG: String = "TravelPlanRepo"
  }

  override suspend fun getCurrentTravelPlan(
    employeeId: String,
    withEntries: Boolean,
  ): Result<TravelPlan?> {
    return try {
      val today = serverTime.todayIn()
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

        Result.Success(travelPlanDto?.toDomain())
      }

    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getTodayTravelPlanEntry(tpId: String): Result<TravelPlanEntry?> {
    return try {
      // TODO: Reset to 0 after testing
      val today = serverTime.todayIn()
        .plus(1, DateTimeUnit.DayBased(1))
      val thisDay = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying travelPlanEntry for current employee and day: $thisDay")

        val travelPlanEntryDto = postgrest.from("travelPlanEntry").select {
          filter {
            TravelPlanEntryDto::tpId eq tpId
            TravelPlanEntryDto::date eq thisDay
          }
        }.decodeSingleOrNull<TravelPlanEntryDto>()

        Result.Success(travelPlanEntryDto?.toDomain())
      }

    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getTravelPlanEntries(tpId: String): Result<List<TravelPlanEntry>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying travelPlanEntries for current employee and tpId: $tpId")

        val travelPlanEntriesDto = postgrest.from("travelPlanEntry").select {
          filter {
            TravelPlanEntryDto::tpId eq tpId
          }
        }.decodeList<TravelPlanEntryDto>()

        val travelPlanEntries = travelPlanEntriesDto.map { it.toDomain() }
        Result.Success(travelPlanEntries)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getTravelPlan(id: String): Result<TravelPlan?> {
    return try {
      withContext(Dispatchers.IO) {
        val travelPlanDto = postgrest.from("travelPlan").select {
          filter { TravelPlanDto::id eq id }
        }.decodeSingleOrNull<TravelPlanDto>()

        Result.Success(travelPlanDto?.toDomain())
      }

    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
