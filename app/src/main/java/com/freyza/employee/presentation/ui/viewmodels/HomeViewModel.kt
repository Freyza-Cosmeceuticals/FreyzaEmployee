package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase.Input
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

    init {
//        loadRecentExpenses()
        loadCurrentTravelPlan()
    }

    fun loadRecentExpenses() {
        viewModelScope.launch {
            val result =
                getRecentExpensesUseCase.execute(GetRecentExpensesUseCase.Input(Constants.NUM_RECENT_EXPENSES))

            when (result) {
                is GetRecentExpensesUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(recentExpenses = Result.Success(result.expenses))
                    }
                    Logger.d(TAG, "${result.expenses.size} Recent Expenses Fetched Successfully")
                }

                is GetRecentExpensesUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(recentExpenses = Result.Error(message = "Unable to fetch recent expenses"))
                    }
                    Logger.e(TAG, "Cannot fetch recent expenses: ${result.message}")
                }
            }
        }
    }

    fun loadCurrentTravelPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentTravelPlan = Result.Loading(it.currentTravelPlan.data)) }

            when (val employeeInfo = getCurrentUserUseCase.execute(Input())) {
                is GetCurrentUserUseCase.Output.Success -> {
                    if (employeeInfo.user !== null) {

                        when (val result = getCurrentTravelPlanUseCase.execute(
                            GetCurrentTravelPlanUseCase.Input(
                                employeeInfo.user.id
                            )
                        )) {
                            is GetCurrentTravelPlanUseCase.Output.Success -> {
                                _uiState.update {
                                    it.copy(currentTravelPlan = Result.Success(result.travelPlan))
                                }
                                Logger.d(
                                    TAG,
                                    "${result.travelPlan?.id} Current Travel Plan Fetched Successfully"
                                )
                            }

                            is GetCurrentTravelPlanUseCase.Output.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        currentTravelPlan = Result.Error(
                                            message = "Unable to fetch current travel plan",
                                            data = it.currentTravelPlan.data
                                        )
                                    )
                                }
                                Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                currentTravelPlan = Result.Error(
                                    message = "Unable to fetch employee id",
                                    data = it.currentTravelPlan.data
                                )
                            )
                        }
                        Logger.e(TAG, "Current User is null")
                    }
                }

                is GetCurrentUserUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            currentTravelPlan = Result.Error(
                                message = "Unable to fetch employee id",
                                data = it.currentTravelPlan.data
                            )
                        )
                    }
                    Logger.e(TAG, "Cannot get current user id")
                }
            }
        }
    }

    fun loadTodayTravelPlanEntry(tpId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todayTravelPlanEntry = Result.Loading(it.todayTravelPlanEntry.data)) }

            when (val result = getTodayTravelPlanEntryUseCase.execute(
                GetTodayTravelPlanEntryUseCase.Input(tpId)
            )) {
                is GetTodayTravelPlanEntryUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(todayTravelPlanEntry = Result.Success(result.travelPlanEntry))
                    }
                    Logger.d(
                        TAG,
                        "${result.travelPlanEntry?.id} Today Travel Plan Entry Fetched Successfully"
                    )
                }

                is GetTodayTravelPlanEntryUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            todayTravelPlanEntry = Result.Error(
                                message = "Unable to fetch today travel plan entry",
                                data = it.todayTravelPlanEntry.data
                            )
                        )
                    }
                    Logger.e(TAG, "Cannot fetch today travel plan entry: ${result.message}")
                }
            }
        }
    }
}
