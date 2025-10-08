package com.freyza.employee.data.repository

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto

interface ExpenseRepository {
    suspend fun createExpense(location: String, distance: Float, cost: Float): Result<ExpenseEntryDto>
    suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>>
    suspend fun lockExpense(id: String): Result<Boolean>
}
