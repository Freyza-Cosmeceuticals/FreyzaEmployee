package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.Expense

interface ExpenseRepository {
    suspend fun createExpense(
        location: String,
        distance: Double,
        cost: Double
    ): Result<Expense>

    suspend fun getAllExpenses(): Result<List<Expense>>
    suspend fun getRecentExpenses(numExpenses: Long): Result<List<Expense>>
    suspend fun getExpense(id: String): Result<Expense>
    suspend fun updateExpense(
        id: String,
        location: String? = null,
        distance: Double? = null,
        cost: Double? = null,
        locked: Boolean? = null
    ): Result<Expense>

    suspend fun lockExpense(id: String): Result<Expense>
}
