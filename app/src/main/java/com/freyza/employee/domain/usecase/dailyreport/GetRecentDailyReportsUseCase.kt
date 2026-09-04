package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.repository.DailyReportRepository

data class GetRecentDailyReportsParams(
  val numDailyReports: Int,
  val employeeId: String,
  val withVisits: Boolean = false,
  val forceRefresh: Boolean = false,
)

class GetRecentDailyReportsUseCase(private val dailyReportRepository: DailyReportRepository) {
  suspend operator fun invoke(params: GetRecentDailyReportsParams): Result<List<DailyReport>> {
    val result = dailyReportRepository.getRecentDailyReports(
      params.numDailyReports,
      params.employeeId,
      params.withVisits,
      params.forceRefresh
    )
    return when (result) {
      is Result.Success -> Result.Success(result.data)
      else -> result
    }
  }
}
