package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.domain.model.dummyUserEmployeeAlt

import kotlinx.datetime.LocalDateTime

data class DailyReportUiState(
  val today: LocalDateTime,
  val dailyReports: UIState<List<DailyReport>> = UIState.Idle(),
  val routes: List<RouteWithLocation> = emptyList(),
  val employees: List<User> = emptyList(),
  val lockingState: UIState<Unit> = UIState.Idle(),
)

fun dummyDailyReportUiState(serverTime: ServerTime = ServerTime()): DailyReportUiState =
  DailyReportUiState(
    today = serverTime.nowLocalDateTime(), dailyReports = UIState.Ready(
      listOf(
        dummyDailyReportWork(dateNow = true),
        dummyDailyReportWork(noVisits = true),
        dummyDailyReportWork(true),
        dummyDailyReportLeave(),
        dummyDailyReportHoliday(),
        dummyDailyReportWork()
      ),
    ), routes = listOf(dummyRouteWithLocation()), employees = listOf(
      dummyUserEmployee(), dummyUserEmployeeAlt()
    ), lockingState = UIState.Idle(Unit)
  )
