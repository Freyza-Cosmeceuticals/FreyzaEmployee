package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlan
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork

data class HomeScreenUiState(
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val todayTravelPlanEntry: UIState<TravelPlanEntry?> = UIState.Idle(),

  val todayPlanEntryRoute: UIState<RouteWithLocation?> = UIState.Idle(),

  val currentDailyReport: UIState<DailyReport?> = UIState.Idle(),
  val routes: UIState<List<RouteWithLocation>> = UIState.Idle(),

  val todayReportDayType: UIState<DayType> = UIState.Idle(),
  val todayReportRoute: UIState<RouteWithLocation?> = UIState.Idle(),
)

fun dummyHomeScreenUiState(): HomeScreenUiState = HomeScreenUiState(
  currentTravelPlan = UIState.Ready(dummyTravelPlan()),
  todayTravelPlanEntry = UIState.Ready(dummyTravelPlanEntryWork()),

  todayPlanEntryRoute = UIState.Ready(dummyRouteWithLocation()),

  currentDailyReport = UIState.Ready(dummyDailyReportWork()),
  routes = UIState.Ready(listOf(dummyRouteWithLocation())),

  todayReportDayType = UIState.Ready(DayType.WORK),
  todayReportRoute = UIState.Ready(dummyRouteWithLocation()),
)

fun dummyHomeScreenUiStateNoDailyReport(): HomeScreenUiState = HomeScreenUiState(
  currentTravelPlan = UIState.Ready(dummyTravelPlan()),
  todayTravelPlanEntry = UIState.Ready(dummyTravelPlanEntryWork()),

  todayPlanEntryRoute = UIState.Ready(dummyRouteWithLocation()),

  currentDailyReport = UIState.Ready(null),
  routes = UIState.Ready(listOf(dummyRouteWithLocation())),
)
