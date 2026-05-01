package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.repository.DailyReportRepository
import kotlinx.datetime.LocalDate

data class GetTodayDailyReportParams(
  val today: LocalDate,
  val employeeId: String,
  val withVisits: Boolean = false,
)

class GetTodayDailyReportUseCase(private val dailyReportRepository: DailyReportRepository) {
  suspend operator fun invoke(params: GetTodayDailyReportParams): Result<DailyReport?> {
    return dailyReportRepository.getTodayDailyReport(
      params.today, params.employeeId, params.withVisits
    )
  }
}
