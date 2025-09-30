package com.freyza.employee.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.data.models.ExpenseEntry
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.util.Result
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _itemsLiveData = MutableLiveData<Result<List<ExpenseEntry>>>()
    val itemsLiveData: LiveData<Result<List<ExpenseEntry>>> = _itemsLiveData

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val items = repository.getAllExpenses()
            _itemsLiveData.postValue(items)
        }
    }

    fun addItem(location: String, distance: Float, cost: Float) {
        viewModelScope.launch {
            val item = repository.createExpense(location, distance, cost)
        }
        loadItems()
    }

    fun lockItem(id: String) {
        viewModelScope.launch {
            repository.lockExpense(id)
        }
        loadItems()
    }
}
