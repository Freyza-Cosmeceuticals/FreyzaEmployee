package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlan
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import kotlinx.datetime.LocalDateTime

data class HomeScreenUiState(
  val isLoading: Boolean = false,
  val isRefreshing: Boolean = false,
  val errorMessage: String? = null,

  val greetingName: String = "",
  val today: LocalDateTime,

  val currentTravelPlan: TravelPlan? = null,
  val todayTravelPlanEntry: TravelPlanEntry? = null,
  val todayPlanEntryRoute: RouteWithLocation? = null,

  val currentDailyReport: DailyReport? = null,
  val routes: List<RouteWithLocation> = emptyList(),
  val locations: List<Location> = emptyList(),

  val todayReportDayType: DayType? = null,
  val todayReportRoute: RouteWithLocation? = null,

  val showCreateReportSheet: Boolean = false,
)

fun dummyHomeScreenUiState(): HomeScreenUiState = HomeScreenUiState(
  today = ServerTime().nowLocalDateTime(),
  currentTravelPlan = dummyTravelPlan(),
  todayTravelPlanEntry = dummyTravelPlanEntryWork(),
  todayPlanEntryRoute = dummyRouteWithLocation(),
  currentDailyReport = dummyDailyReportWork(),
  routes = listOf(dummyRouteWithLocation()),
  todayReportDayType = DayType.WORK,
  todayReportRoute = dummyRouteWithLocation(),
)

fun dummyHomeScreenUiStateDailyReportError(): HomeScreenUiState = HomeScreenUiState(
  today = ServerTime().nowLocalDateTime(), errorMessage = "Failed to have a daily report"
)
