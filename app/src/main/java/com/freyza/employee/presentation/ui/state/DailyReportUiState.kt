package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation

data class DailyReportUiState(
  val dailyReports: UIState<List<DailyReport>> = UIState.Idle(),
  val routes: UIState<List<RouteWithLocation>> = UIState.Idle(),
)

fun dummyDailyReportUiState(): DailyReportUiState = DailyReportUiState(
  dailyReports = UIState.Ready(
    listOf(
      dummyDailyReportWork(), dummyDailyReportLeave(),
      dummyDailyReportHoliday()
    )
  ),
  routes = UIState.Ready(listOf(dummyRouteWithLocation()))
)
