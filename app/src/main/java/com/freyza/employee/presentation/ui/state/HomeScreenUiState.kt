package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

data class HomeScreenUiState(
  val recentExpenses: UIState<List<Expense>> = UIState.Idle(),
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val todayTravelPlanEntry: UIState<TravelPlanEntry?> = UIState.Idle(),

  val todayPlanEntryRoute: UIState<RouteWithLocation?> = UIState.Idle(),

  val currentDailyReport: UIState<DailyReport?> = UIState.Idle(),
  val routes: UIState<List<RouteWithLocation>> = UIState.Idle(),

  val todayReportDayType: UIState<DayType> = UIState.Idle(),
  val todayReportRoute: UIState<RouteWithLocation?> = UIState.Idle(),
)
