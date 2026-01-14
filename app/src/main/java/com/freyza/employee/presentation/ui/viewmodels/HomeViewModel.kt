package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getRecentExpensesUseCase: GetRecentExpensesUseCase,
    private val getCurrentTravelPlanUseCase: GetCurrentTravelPlanUseCase,
    private val getTodayTravelPlanEntryUseCase: GetTodayTravelPlanEntryUseCase
) : ViewModel() {

    companion object {
        const val TAG = "HOME_VIEW_MODEL"
    }

    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun loadRecentExpenses() {
        viewModelScope.launch {
            val result =
                getRecentExpensesUseCase.execute(GetRecentExpensesUseCase.Input(Constants.NUM_RECENT_EXPENSES))

            when (result) {
                is GetRecentExpensesUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(recentExpenses = UIState.Ready(result.expenses))
                    }
                    Logger.d(TAG, "${result.expenses.size} Recent Expenses Fetched Successfully")
                }

                is GetRecentExpensesUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(recentExpenses = UIState.Error(message = "Unable to fetch recent expenses"))
                    }
                    Logger.e(TAG, "Cannot fetch recent expenses: ${result.message}")
                }
            }
        }
    }

    fun loadCurrentTravelPlan(employeeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(currentTravelPlan = UIState.Loading(it.currentTravelPlan.data)) }
            when (val result = getCurrentTravelPlanUseCase.execute(
                GetCurrentTravelPlanUseCase.Input(
                    employeeId
                )
            )) {
                is GetCurrentTravelPlanUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(currentTravelPlan = UIState.Ready(result.travelPlan))
                    }
                    Logger.d(
                        TAG,
                        "${result.travelPlan?.id} Current Travel Plan Fetched Successfully"
                    )

                    // fetch today's entry
                    if (result.travelPlan?.id !== null) loadTodayTravelPlanEntry(result.travelPlan.id)
                }

                is GetCurrentTravelPlanUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            currentTravelPlan = UIState.Error(message = "Unable to fetch current travel plan")
                        )
                    }
                    Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
                }
            }
        }

    }

    fun loadTodayTravelPlanEntry(tpId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todayTravelPlanEntry = UIState.Loading(it.todayTravelPlanEntry.data)) }

            when (val result = getTodayTravelPlanEntryUseCase.execute(
                GetTodayTravelPlanEntryUseCase.Input(tpId)
            )) {
                is GetTodayTravelPlanEntryUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(todayTravelPlanEntry = UIState.Ready(result.travelPlanEntry))
                    }
                    Logger.d(
                        TAG,
                        "${result.travelPlanEntry?.id} Today Travel Plan Entry Fetched Successfully"
                    )
                }

                is GetTodayTravelPlanEntryUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            todayTravelPlanEntry = UIState.Error(message = "Unable to fetch today travel plan entry")
                        )
                    }
                    Logger.e(TAG, "Cannot fetch today travel plan entry: ${result.message}")
                }
            }
        }
    }
}
