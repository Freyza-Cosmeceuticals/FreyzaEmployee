package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitUpdateDto
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Visit
import kotlinx.datetime.LocalDate

interface DailyReportRepository {

  suspend fun getTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    withVisits: Boolean = false,
  ): Result<DailyReport?>

  suspend fun getRecentDailyReports(
    numDailyReports: Int,
    employeeId: String,
    withVisits: Boolean = false,
  ): Result<List<DailyReport>>

  suspend fun getVisits(dailyReportId: String): Result<List<Visit>>
  suspend fun getDailyReport(id: String, withVisits: Boolean = false): Result<DailyReport?>
  suspend fun getVisit(id: String): Result<Visit?>

  suspend fun createTodayDailyReport(
    today: LocalDate,
    employeeId: String,
    dayType: DayType,
    routeId: String?,
    travellingWithId: String?,
  ): Result<DailyReport>

  suspend fun createVisit(
    today: LocalDate,
    employeeId: String,
    dailyReportId: String,
    visitCreateDto: VisitCreateDto,
  ): Result<Visit>

  suspend fun lockReport(
    reportId: String,
  ): Result<Boolean>

  suspend fun deleteVisit(
    visitId: String,
  ): Result<Boolean>

  suspend fun updateVisit(
    visitId: String,
    visitUpdateDto: VisitUpdateDto,
  ): Result<Visit>
}
