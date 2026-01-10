package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry

data class HomeScreenUiState(
    val recentExpenses: Result<List<Expense>> = Result.Success(listOf()),
    val currentTravelPlan: Result<TravelPlan?> = Result.Success(null),
    val todayTravelPlanEntry: Result<TravelPlanEntry?> = Result.Success(null)
)
