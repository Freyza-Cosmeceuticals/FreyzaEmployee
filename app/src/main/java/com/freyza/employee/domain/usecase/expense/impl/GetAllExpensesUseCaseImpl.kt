package com.freyza.employee.domain.usecase.expense.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.domain.usecase.expense.GetAllExpensesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllExpensesUseCaseImpl(private val expenseRepository: ExpenseRepository) :
    GetAllExpensesUseCase {
    override suspend fun execute(input: GetAllExpensesUseCase.Input): GetAllExpensesUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = expenseRepository.getAllExpenses()
            when (result) {
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
