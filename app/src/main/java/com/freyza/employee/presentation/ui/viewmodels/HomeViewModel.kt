package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Constants
import com.freyza.employee.common.Result
import com.freyza.employee.domain.model.HomeScreenUiState
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val getRecentExpensesUseCase: GetRecentExpensesUseCase) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow<Result<HomeScreenUiState>>(
        Result.Loading(
            HomeScreenUiState()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadRecentExpenses()
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
}
