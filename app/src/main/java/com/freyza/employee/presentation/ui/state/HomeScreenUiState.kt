package com.freyza.employee.presentation.ui.state

import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.TravelPlan

data class HomeScreenUiState(
    val recentExpenses: List<Expense> = listOf(),
    val currentTravelPlan: TravelPlan? = null
)
