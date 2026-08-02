package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.repository.DailyReportRepository
import kotlinx.datetime.LocalDate

data class CreateTodayDailyReportParams(
  val today: LocalDate,
  val employeeId: String,
  val dayType: DayType,
  val routeId: String?,
  val travellingWithId: String?,
)

class CreateTodayDailyReportUseCase(private val dailyReportRepository: DailyReportRepository) {
  suspend operator fun invoke(params: CreateTodayDailyReportParams): Result<DailyReport> {
    return dailyReportRepository.createTodayDailyReport(
      params.today,
      params.employeeId,
      params.dayType,
      params.routeId,
      params.travellingWithId
    )
  }
}
