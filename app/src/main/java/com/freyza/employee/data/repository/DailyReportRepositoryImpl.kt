package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.DailyReportCreateDto
import com.freyza.employee.data.network.dto.DailyReportDto
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.data.network.dto.VisitUpdateDto
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.repository.DailyReportRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class DailyReportRepositoryImpl(private val postgrest: Postgrest) : DailyReportRepository {
  companion object {
    const val TAG: String = "DailyReportRepo"
    private const val TABLE_DAILY_REPORT = "dailyReport"
    private const val TABLE_VISIT = "visit"
    private const val SELECT_WITH_VISITS = "*, visits:visit!visit_reportId_fkey(*)"
    private const val SELECT_ALL = "*"
  }

  override suspend fun getTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    withVisits: Boolean,
  ): Result<DailyReport?> {
    return try {
      val thisDate = DateFormatter.format(today, DateFormatter.FormattingType.MACHINE)
      val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

      withContext(Dispatchers.IO) {
        Logger.d(
          TAG,
          "Querying dailyReport for current employee and date: $thisDate, withVisits=${withVisits}"
        )

        val dailyReportDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::employeeId eq employeeId
            DailyReportDto::date eq today
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.toDomain()
        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getRecentDailyReports(
    numDailyReports: Int,
    employeeId: String,
    withVisits: Boolean,
  ): Result<List<DailyReport>> {
    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying recent daily reports withVisits=${withVisits}")

        val reportsDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::employeeId eq employeeId
          }
          order(DailyReportDto::date.name, Order.DESCENDING)
          limit(numDailyReports.toLong())
        }.decodeList<DailyReportDto>()

        val reports = reportsDto.map { it.toDomain() }
        Result.Success(reports)
      }
    } catch (e: Exception) {
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getVisits(dailyReportId: String): Result<List<Visit>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visits for current employee and dailyReportId: $dailyReportId")

        val visitsDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::reportId eq dailyReportId
          }
        }.decodeList<VisitDto>()

        val visits = visitsDto.map { it.toDomain() }
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
  ): Result<DailyReport?> {
    val selectQuery = if (withVisits) SELECT_WITH_VISITS else SELECT_ALL

    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying dailyReport with id: $id withVisits=$withVisits")

        val dailyReportDto = postgrest.from(TABLE_DAILY_REPORT).select(
          columns = Columns.raw(selectQuery)
        ) {
          filter {
            DailyReportDto::id eq id
          }
        }.decodeSingleOrNull<DailyReportDto>()

        val dailyReport = dailyReportDto?.toDomain()
        Result.Success(dailyReport)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getVisit(id: String): Result<Visit?> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying visit with id: $id")

        val visitDto = postgrest.from(TABLE_VISIT).select {
          filter {
            VisitDto::id eq id
          }
        }.decodeSingleOrNull<VisitDto>()

        val visit = visitDto?.toDomain()
        Result.Success(visit)
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

        val dailyReportDto = postgrest.from(TABLE_DAILY_REPORT).insert(initialDailyReportDto) {
          select()
        }.decodeSingle<DailyReportDto>()

        Result.Success(dailyReportDto.toDomain())
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun createVisit(
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

        val visitDto = postgrest.from(TABLE_VISIT).insert(visitCreateDto) {
          select()
        }.decodeSingle<VisitDto>()

        Result.Success(visitDto.toDomain())
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun lockReport(reportId: String): Result<Boolean> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Locking report:$reportId")

        postgrest.from(TABLE_DAILY_REPORT).update(
          {
            DailyReportDto::locked setTo true
          }
        ) {
          filter {
            DailyReportDto::id eq reportId
          }
        }

        Result.Success(true)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun deleteVisit(visitId: String): Result<Boolean> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Deleting visit:$visitId")

        postgrest.from(TABLE_VISIT).delete {
          filter {
            VisitDto::id eq visitId
          }
        }

        Result.Success(true)
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun updateVisit(
    visitId: String,
    visitUpdateDto: VisitUpdateDto,
  ): Result<Visit> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Updating visit:$visitId")

        val visitDto = postgrest.from(TABLE_VISIT).update(visitUpdateDto) {
          filter {
            VisitDto::id eq visitId
          }
          select()
        }.decodeSingle<VisitDto>()

        Result.Success(visitDto.toDomain())
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
