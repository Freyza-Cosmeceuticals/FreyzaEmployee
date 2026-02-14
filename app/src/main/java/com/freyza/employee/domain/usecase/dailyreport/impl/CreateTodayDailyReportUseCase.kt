package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.usecase.UseCase
import kotlinx.datetime.LocalDate

interface CreateTodayDailyReportUseCase :
  UseCase<CreateTodayDailyReportUseCase.Input, CreateTodayDailyReportUseCase.Output> {
  class Input(
    val today: LocalDate,
    val employeeId: String,
    val dayType: DayType,
    val routeId: String?,
  )

  sealed class Output() {
    data class Success(val dailyReport: DailyReport?) : Output()
    data class Failure(val message: String) : Output()
  }
}
