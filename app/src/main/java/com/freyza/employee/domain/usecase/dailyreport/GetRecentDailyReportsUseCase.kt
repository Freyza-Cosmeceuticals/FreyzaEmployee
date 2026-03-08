package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.usecase.UseCase

interface GetRecentDailyReportsUseCase :
  UseCase<GetRecentDailyReportsUseCase.Input, GetRecentDailyReportsUseCase.Output> {
  class Input(val numDailyReports: Int, val employeeId: String, val withVisits: Boolean = false)

  sealed class Output() {
    data class Success(val dailyReports: List<DailyReport>) : Output()
    data class Failure(val message: String) : Output()
  }
}
