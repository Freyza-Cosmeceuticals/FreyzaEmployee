package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

data class HomeScreenUiState(
    val recentExpenses: UIState<List<Expense>> = UIState.Idle(),
    val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
    val todayTravelPlanEntry: UIState<TravelPlanEntry?> = UIState.Idle(),
    val todayPlanEntryRoute: UIState<Route?> = UIState.Idle(),
    val todayPlanEntrySrcDest: UIState<Pair<Location, Location>?> = UIState.Idle()
)
