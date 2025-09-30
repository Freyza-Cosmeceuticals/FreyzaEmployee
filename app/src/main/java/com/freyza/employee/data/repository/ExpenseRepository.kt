package com.freyza.employee.data.repository

import com.freyza.employee.data.models.ExpenseEntry
import com.freyza.employee.util.Result

interface ExpenseRepository {
    suspend fun createExpense(location: String, distance: Float, cost: Float): Result<ExpenseEntry>
    suspend fun getAllExpenses(): Result<List<ExpenseEntry>>
    suspend fun lockExpense(id: String): Result<Boolean>
}