package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
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
) : ViewModel() {

    companion object {
        const val TAG = "HOME_VIEW_MODEL"
    }

    private val _uiState = MutableStateFlow<Result<HomeScreenUiState>>(
        Result.Loading(
            HomeScreenUiState()
        )
    )
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
                        Result.Success(it.data?.copy(recentExpenses = result.expenses))
                    }
                    Logger.d(TAG, "${result.expenses.size} Recent Expenses Fetched Successfully")
                }

                is GetRecentExpensesUseCase.Output.Failure -> {
                    _uiState.update {
                        Result.Error(message = "Unable to fetch recent expenses", it.data?.copy())
                    }
                    Logger.e(TAG, "Cannot fetch recent expenses: ${result.message}")
                }
            }
        }
    }

    fun loadCurrentTravelPlan() {
        viewModelScope.launch {

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
                                    Result.Success(it.data?.copy(currentTravelPlan = result.travelPlan))
                                }
                                Logger.d(
                                    TAG,
                                    "${result.travelPlan?.id} Current Travel Plan Fetched Successfully"
                                )
                            }

                            is GetCurrentTravelPlanUseCase.Output.Failure -> {
                                _uiState.update {
                                    Result.Error(
                                        message = "Unable to fetch current travel plan",
                                        it.data?.copy()
                                    )
                                }
                                Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
                            }

                        }
                    } else {
                        _uiState.update {
                            Result.Error("Unable to fetch employee id")
                        }
                        Logger.e(TAG, "Current User is null")
                    }
                }

                is GetCurrentUserUseCase.Output.Failure -> {
                    _uiState.update {
                        Result.Error("Unable to fetch employee id")
                    }
                    Logger.e(TAG, "Cannot get current user id")
                }
            }
        }
    }
}
