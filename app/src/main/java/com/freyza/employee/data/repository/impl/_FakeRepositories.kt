package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto
import com.freyza.employee.data.repository.ExpenseRepository
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

class FakeExpenseRepository : ExpenseRepository {
    private val items = mutableListOf<ExpenseEntryDto>(
        ExpenseEntryDto(
            "13412eb6-8a56-47aa-812e-b68a5637aaf2",
            "Patna",
            120f,
            240f,
            true
        ),
        ExpenseEntryDto(
            "45987a6b-cc2f-482d-987a-6bcc2fb82db8",
            "Darbhanga",
            520f,
            1040f
        )
    )

    override suspend fun createExpense(
        location: String,
        distance: Float,
        cost: Float
    ): Result<ExpenseEntryDto> {
        delay(2500)

        return if (Random.nextBoolean()) {
            val expense = ExpenseEntryDto(UUID.randomUUID().toString(), location, distance, cost)
            items.add(expense)

            Result.Success(expense)
        } else {
            Result.Error(Exception("Network Error"))
        }
    }

    override suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>> {
        delay(1000)

        return if (Random.nextBoolean()) {
            Result.Success(items.toList())
        } else {
            Result.Error(Exception("Network Error"))
        }
    }

    override suspend fun lockExpense(id: String): Result<Boolean> {
        val ex = items.find { (_id, _, _, _, _) -> _id == id }
        if (ex !== null) {
            ex.locked = true
        }

        return Result.Success(true)
    }
}
