package com.freyza.employee.domain.usecase.expense.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.ExpenseRepository
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetRecentExpensesUseCaseImpl(private val expenseRepository: ExpenseRepository) :
    GetRecentExpensesUseCase {
    override suspend fun execute(input: GetRecentExpensesUseCase.Input): GetRecentExpensesUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = expenseRepository.getRecentExpenses(input.numExpense)) {
                is Result.Success -> {
                    GetRecentExpensesUseCase.Output.Success(result.data ?: listOf())
                }

                is Result.Error -> {
                    GetRecentExpensesUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetRecentExpensesUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }
}
