package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

data class TravelPlanUiState(
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val travelPlanEntries: UIState<List<TravelPlanEntry>> = UIState.Idle(),
)
