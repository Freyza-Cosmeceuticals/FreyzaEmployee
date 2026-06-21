package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import kotlinx.datetime.LocalDateTime

data class TravelPlanUiState(
  val today: LocalDateTime,
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val travelPlanEntries: UIState<List<TravelPlanEntry>> = UIState.Idle(),

  val routes: List<RouteWithLocation>? = null,

  val selectedRoute: UIState<RouteWithLocation?> = UIState.Idle(),
)
