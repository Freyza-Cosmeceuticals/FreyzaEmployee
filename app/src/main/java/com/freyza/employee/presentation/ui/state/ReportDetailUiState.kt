package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.domain.model.dummyUserEmployeeAlt
import kotlinx.datetime.LocalDateTime

data class ReportDetailUiState(
  val today: LocalDateTime,
  val reportId: String,
  val report: UIState<DailyReport?> = UIState.Idle(),
  val routes: List<RouteWithLocation> = listOf(),
  val employees: List<User> = listOf(),
  val lockingState: UIState<Unit> = UIState.Idle(),
)

fun dummyReportDetailUiState(
  serverTime: ServerTime = ServerTime(),
  dateNow: Boolean = false,
  noVisits: Boolean = false,
) = ReportDetailUiState(
  today = serverTime.nowLocalDateTime(),
  reportId = "fdc8b26f-9a2c-4789-88b2-6f9a2cf789b8",
  report = UIState.Ready(dummyDailyReportWork(dateNow = dateNow, noVisits = noVisits)),
  employees = listOf(dummyUserEmployee(), dummyUserEmployeeAlt()),
  routes = listOf(dummyRouteWithLocation())
)
