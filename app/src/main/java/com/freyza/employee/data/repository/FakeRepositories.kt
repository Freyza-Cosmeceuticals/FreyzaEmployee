package com.freyza.employee.data.repository

import com.freyza.employee.data.models.ExpenseEntry
import com.freyza.employee.data.models.User
import com.freyza.employee.util.Result
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.random.Random

class FakeUserRepository : UserRepository {
    override suspend fun login(username: String, password: String): Result<User> {
        delay(2000)
        return if (username == "ram" && password == "pass") {
            Result.Success(
                User(
                    "316d511d-76e9-44ff-ad51-1d76e9c4ff59",
                    "Ram",
                    "ram@gmail.com"
                )
            )
        } else {
            Result.Error(Exception("Invalid Credentials"))
        }
    }

    override suspend fun logout() {
        delay(1000)
    }
}

class FakeExpenseRepository : ExpenseRepository {
    private val items = mutableListOf<ExpenseEntry>(
        ExpenseEntry(
            "13412eb6-8a56-47aa-812e-b68a5637aaf2",
            "Patna",
            120f,
            240f,
            true
        ),
        ExpenseEntry(
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
    ): Result<ExpenseEntry> {
        delay(2500)

        return if (Random.nextBoolean()) {
            val expense = ExpenseEntry(UUID.randomUUID().toString(), location, distance, cost)
            items.add(expense)

            Result.Success(expense)
        } else {
            Result.Error(Exception("Network Error"))
        }
    }

    override suspend fun getAllExpenses(): Result<List<ExpenseEntry>> {
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
