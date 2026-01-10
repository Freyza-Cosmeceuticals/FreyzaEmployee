package com.freyza.employee.presentation.ui.state

import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

data class HomeScreenUiState(
    val recentExpenses: List<Expense> = listOf(),
    val currentTravelPlan: TravelPlan? = null,
    val todayTravelPlanEntry: TravelPlanEntry? = null
)
