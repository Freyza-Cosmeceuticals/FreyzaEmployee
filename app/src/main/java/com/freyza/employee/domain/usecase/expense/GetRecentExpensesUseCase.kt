package com.freyza.employee.domain.usecase.expense

import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.usecase.UseCase

interface GetRecentExpensesUseCase :
    UseCase<GetRecentExpensesUseCase.Input, GetRecentExpensesUseCase.Output> {
    class Input(val numExpense: Long)

    sealed class Output() {
        data class Success(val expenses: List<Expense>) : Output()
        data class Failure(val message: String) : Output()
    }
}
