package com.freyza.employee.data.repository

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto

interface ExpenseRepository {
    suspend fun createExpense(
        location: String,
        distance: Float,
        cost: Float
    ): Result<ExpenseEntryDto>

    suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>>
    suspend fun getExpense(id: String): Result<ExpenseEntryDto>
    suspend fun updateExpense(
        id: String,
        location: String? = null,
        distance: Float? = null,
        cost: Float? = null,
        locked: Boolean? = null
    ): Result<ExpenseEntryDto>

    suspend fun lockExpense(id: String): Result<ExpenseEntryDto>
}
