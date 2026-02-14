package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.DailyReportCreateDto
import com.freyza.employee.data.network.dto.DailyReportDto
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.repository.DailyReportRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class DailyReportRepositoryImpl(private val postgrest: Postgrest) : DailyReportRepository {
  companion object {
    const val TAG: String = "DailyReportRepo"
  }

  override suspend fun getTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    withVisits: Boolean,
  ): Result<DailyReport> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying dailyReport for current employee and date: $thisDate")

        val dailyReportDto = postgrest.from("dailyReport").select {
          filter {
            DailyReportDto::employeeId eq employeeId
            DailyReportDto::date eq today
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.let { reportDto ->
          DailyReport(
            id = reportDto.id,
            employeeId = reportDto.employeeId,
            date = LocalDate.parse(reportDto.date),
            dayType = reportDto.dayType,
            routeId = reportDto.routeId,
            ta = reportDto.ta,
            da = reportDto.da,
            totalExpense = reportDto.totalExpense,
            visits = listOf(),
            locked = reportDto.locked,
            lockedAt = reportDto.lockedAt?.let { Instant.parse(it) },
            createdAt = Instant.parse(reportDto.createdAt),
            updatedAt = reportDto.updatedAt?.let { Instant.parse(it) })
        }

        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getVisits(dailyReportId: String): Result<List<Visit>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visits for current employee and dailyReportId: $dailyReportId")

        val visitsDto = postgrest.from("visit").select {
          filter {
            VisitDto::reportId eq dailyReportId
          }
        }.decodeList<VisitDto>()

        val visits = visitsDto.map {
          Visit(
            id = it.id,
            reportId = it.reportId,
            visitType = it.visitType,
            latitude = it.latitude,
            longitude = it.longitude,
            distanceMetersFromPOI = it.distanceMetersFromPOI,
            createdAt = Instant.parse(it.createdAt),
            updatedAt = it.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
          )
        }

        Result.Success(visits)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getDailyReport(
    id: String,
    withVisits: Boolean,
  ): Result<DailyReport> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying dailyReport with id: $id")

        val dailyReportDto = postgrest.from("dailyReport").select {
          filter {
            DailyReportDto::id eq id
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.let { reportDto ->
          DailyReport(
            id = reportDto.id,
            employeeId = reportDto.employeeId,
            date = LocalDate.parse(reportDto.date),
            dayType = reportDto.dayType,
            routeId = reportDto.routeId,
            ta = reportDto.ta,
            da = reportDto.da,
            totalExpense = reportDto.totalExpense,
            visits = listOf(),
            locked = reportDto.locked,
            lockedAt = reportDto.lockedAt?.let { Instant.parse(it) },
            createdAt = Instant.parse(reportDto.createdAt),
            updatedAt = reportDto.updatedAt?.let { Instant.parse(it) })
        }

        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun createTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    dayType: DayType,
    routeId: String?,
  ): Result<DailyReport> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Creating dailyReport for current employee and date: $thisDate")

        val initialDailyReportDto = DailyReportCreateDto(
          employeeId = employeeId,
          date = thisDate,
          dayType = dayType,
          routeId = routeId,
        )

        val dailyReportDto = postgrest.from("dailyReport").insert(initialDailyReportDto) {
          select()
        }.decodeSingle<DailyReportDto>()

        val dailyReport = DailyReport(
          id = dailyReportDto.id,
          employeeId = dailyReportDto.employeeId,
          date = LocalDate.parse(dailyReportDto.date),
          dayType = dailyReportDto.dayType,
          routeId = dailyReportDto.routeId,
          ta = dailyReportDto.ta,
          da = dailyReportDto.da,
          totalExpense = dailyReportDto.totalExpense,
          visits = listOf(),
          locked = dailyReportDto.locked,
          lockedAt = dailyReportDto.lockedAt?.let { Instant.parse(it) },
          createdAt = Instant.parse(dailyReportDto.createdAt),
          updatedAt = dailyReportDto.updatedAt?.let { Instant.parse(it) })

        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun createTodayVisit(
    today: LocalDate,
    employeeId: String,
    dailyReportId: String,
    visitCreateDto: VisitCreateDto,
  ): Result<Visit> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)

      withContext(Dispatchers.IO) {
        Logger.d(
          TAG,
          "Creating visit for current employee, dailyReportId: $dailyReportId and date: $thisDate"
        )

        val visitDto = postgrest.from("visit").insert(visitCreateDto) {
          select()
        }.decodeSingle<VisitDto>()

        val visits = Visit(
          id = visitDto.id,
          reportId = visitDto.reportId,
          visitType = visitDto.visitType,
          latitude = visitDto.latitude,
          longitude = visitDto.longitude,
          distanceMetersFromPOI = visitDto.distanceMetersFromPOI,
          createdAt = Instant.parse(visitDto.createdAt),
          updatedAt = visitDto.updatedAt?.let { updatedAt -> Instant.parse(updatedAt) },
        )

        Result.Success(visits)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
