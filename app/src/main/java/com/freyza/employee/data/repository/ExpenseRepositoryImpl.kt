package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto
import com.freyza.employee.domain.repository.ExpenseRepository
import com.freyza.employee.domain.model.Expense
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepositoryImpl(private val postrest: Postgrest) : ExpenseRepository {
    override suspend fun createExpense(
        location: String,
        distance: Double,
        cost: Double
    ): Result<Expense> {
        return try {
            withContext(Dispatchers.IO) {
                val expenseDto =
                    ExpenseEntryDto(location = location, distance = distance, cost = cost)
                val createdExpenseDto = postrest.from("expenses").insert(expenseDto) {
                    select()
                }.decodeSingle<ExpenseEntryDto>()

                val expense = Expense(
                    id = createdExpenseDto.id!!,
                    location = createdExpenseDto.location,
                    distance = createdExpenseDto.distance,
                    cost = createdExpenseDto.cost,
                    locked = createdExpenseDto.locked
                )

                Result.Success(expense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getAllExpenses(): Result<List<Expense>> {
        return try {
            withContext(Dispatchers.IO) {
                val expensesDto = postrest.from("expenses").select().decodeList<ExpenseEntryDto>()
                val expenses = expensesDto.map {
                    Expense(
                        id = it.id!!,
                        location = it.location,
                        distance = it.distance,
                        cost = it.cost,
                        locked = it.locked
                    )
                }

                Result.Success(expenses)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getRecentExpenses(numExpenses: Long): Result<List<Expense>> {
        return try {
            withContext(Dispatchers.IO) {
                val expensesDto = postrest.from("expenses").select() {
                    limit(count = numExpenses)
                }.decodeList<ExpenseEntryDto>()
                val expenses = expensesDto.map {
                    Expense(
                        id = it.id!!,
                        location = it.location,
                        distance = it.distance,
                        cost = it.cost,
                        locked = it.locked
                    )
                }

                Result.Success(expenses)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getExpense(id: String): Result<Expense> {
        return try {
            withContext(Dispatchers.IO) {
                val expenseDto = postrest.from("expenses").select {
                    filter {
                        ExpenseEntryDto::id eq id
                    }
                }.decodeSingleOrNull<ExpenseEntryDto>()

                val expense = expenseDto?.let {
                    Expense(
                        id = it.id!!,
                        location = it.location,
                        distance = it.distance,
                        cost = it.cost,
                        locked = it.locked
                    )
                }

                Result.Success(expense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun updateExpense(
        id: String,
        location: String?,
        distance: Double?,
        cost: Double?,
        locked: Boolean?
    ): Result<Expense> {
        return try {

            val oldExpense = getExpense(id)
            if (oldExpense.data?.locked == true) {
                return Result.Error("Locked Expense cannot be updated")
            }

            withContext(Dispatchers.IO) {
                val updatedExpenseDto = postrest.from("expenses").update({
                    if (location != null)
                        ExpenseEntryDto::location setTo location

                    if (distance != null)
                        ExpenseEntryDto::distance setTo distance

                    if (cost != null)
                        ExpenseEntryDto::cost setTo cost

                    if (locked != null)
                        ExpenseEntryDto::locked setTo locked
                }) {
                    select()
                    filter {
                        ExpenseEntryDto::id eq id
                    }
                }.decodeSingle<ExpenseEntryDto>()

                val updatedExpense = updatedExpenseDto.let {
                    Expense(
                        id = it.id!!,
                        location = it.location,
                        distance = it.distance,
                        cost = it.cost,
                        locked = it.locked
                    )
                }

                Result.Success(updatedExpense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun lockExpense(id: String): Result<Expense> {
        return try {
            updateExpense(id, locked = true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }
}
