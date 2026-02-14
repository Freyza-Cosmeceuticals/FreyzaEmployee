package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.usecase.UseCase
import kotlinx.datetime.LocalDate

interface GetTodayDailyReportUseCase :
  UseCase<GetTodayDailyReportUseCase.Input, GetTodayDailyReportUseCase.Output> {
  class Input(val today: LocalDate, val employeeId: String)

  sealed class Output() {
    data class Success(val dailyReport: DailyReport?) : Output()
    data class Failure(val message: String) : Output()
  }
}
