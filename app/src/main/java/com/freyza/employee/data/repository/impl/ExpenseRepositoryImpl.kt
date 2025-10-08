package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto
import com.freyza.employee.data.repository.ExpenseRepository
import io.github.jan.supabase.postgrest.Postgrest

class ExpenseRepositoryImpl(private val postrest: Postgrest): ExpenseRepository {
    override suspend fun createExpense(
        location: String,
        distance: Float,
        cost: Float
    ): Result<ExpenseEntryDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>> {
        TODO("Not yet implemented")
    }

    override suspend fun lockExpense(id: String): Result<Boolean> {
        TODO("Not yet implemented")
    }
}
