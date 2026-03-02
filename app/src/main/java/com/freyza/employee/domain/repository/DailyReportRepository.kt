package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Visit
import kotlinx.datetime.LocalDate

interface DailyReportRepository {

  suspend fun getTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    withVisits: Boolean = false,
  ): Result<DailyReport>

  suspend fun getRecentDailyReports(
    numDailyReports: Int,
    employeeId: String,
  ): Result<List<DailyReport>>

  suspend fun getVisits(dailyReportId: String): Result<List<Visit>>
  suspend fun getDailyReport(id: String, withVisits: Boolean = false): Result<DailyReport>

  suspend fun createTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    dayType: DayType,
    routeId: String?,
  ): Result<DailyReport>

  suspend fun createTodayVisit(
    today: LocalDate,
    employeeId: String,
    dailyReportId: String,
    visitCreateDto: VisitCreateDto,
  ): Result<Visit>
}
