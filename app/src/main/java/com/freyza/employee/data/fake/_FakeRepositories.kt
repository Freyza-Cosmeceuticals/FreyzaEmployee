package com.freyza.employee.data.fake

import com.freyza.employee.core.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

class FakeExpenseRepository {
    //: ExpenseRepository {
    private val items = mutableListOf<ExpenseEntryDto>(
        ExpenseEntryDto(
            "13412eb6-8a56-47aa-812e-b68a5637aaf2",
            "Patna",
            120.0,
            240.0,
            true
        ),
        ExpenseEntryDto(
            "45987a6b-cc2f-482d-987a-6bcc2fb82db8",
            "Darbhanga",
            520.0,
            1040.0
        )
    )

    suspend fun createExpense(
        location: String,
        distance: Double,
        cost: Double
    ): Result<ExpenseEntryDto> {
        delay(2500)

        return if (Random.nextBoolean()) {
            val expense = ExpenseEntryDto(UUID.randomUUID().toString(), location, distance, cost)
            items.add(expense)

            Result.Success(expense)
        } else {
            Result.Error("Network Error")
        }
    }

    suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>> {
        delay(1000)

        return if (Random.nextBoolean()) {
            Result.Success(items.toList())
        } else {
            Result.Error("Network Error")
        }
    }

    suspend fun lockExpense(id: String): Result<Boolean> {
        val ex = items.find { (_id, _, _, _, _) -> _id == id }
        if (ex !== null) {
            ex.locked = true
        }

        return Result.Success(true)
    }
}
