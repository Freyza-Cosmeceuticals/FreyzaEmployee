package com.freyza.employee.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.data.models.ExpenseEntry
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Result<List<ExpenseEntry>>>(Result.Success(emptyList()))
    val itemsData: StateFlow<Result<List<ExpenseEntry>>> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val items = expenseRepository.getAllExpenses()
            _uiState.value = items
        }
    }

    fun addItem(location: String, distance: Float, cost: Float) {
        viewModelScope.launch {
            val item = expenseRepository.createExpense(location, distance, cost)
        }
        loadItems()
    }

    fun lockItem(id: String) {
        viewModelScope.launch {
            expenseRepository.lockExpense(id)
        }
        loadItems()
    }
}
