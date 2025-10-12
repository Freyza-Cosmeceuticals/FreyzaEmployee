package com.freyza.employee.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Result
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.domain.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Result<List<Expense>>>(Result.Success(emptyList()))
    val itemsData: StateFlow<Result<List<Expense>>> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val items = expenseRepository.getAllExpenses()
            _uiState.value = items
        }
    }

    fun addItem(location: String, distance: Double, cost: Double) {
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
