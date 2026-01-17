package com.freyza.employee.domain.usecase.expense.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.ExpenseRepository
import com.freyza.employee.domain.usecase.expense.GetAllExpensesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllExpensesUseCaseImpl(private val expenseRepository: ExpenseRepository) :
    GetAllExpensesUseCase {
    override suspend fun execute(input: GetAllExpensesUseCase.Input): GetAllExpensesUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = expenseRepository.getAllExpenses()) {
                is Result.Success -> {
                    GetAllExpensesUseCase.Output.Success(result.data ?: listOf())
                }

                is Result.Error -> {
                    GetAllExpensesUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetAllExpensesUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }
}
