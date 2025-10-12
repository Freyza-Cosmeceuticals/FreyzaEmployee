package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.ExpenseEntryDto
import com.freyza.employee.data.repository.ExpenseRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepositoryImpl(private val postrest: Postgrest) : ExpenseRepository {
    override suspend fun createExpense(
        location: String,
        distance: Float,
        cost: Float
    ): Result<ExpenseEntryDto> {
        return try {
            withContext(Dispatchers.IO) {
                val expenseDto =
                    ExpenseEntryDto(location = location, distance = distance, cost = cost)
                val expense = postrest.from("expenses").insert(expenseDto) {
                    select()
                }.decodeSingle<ExpenseEntryDto>()

                Result.Success(expense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getAllExpenses(): Result<List<ExpenseEntryDto>> {
        return try {
            withContext(Dispatchers.IO) {
                val expenses = postrest.from("expenses").select().decodeList<ExpenseEntryDto>()

                Result.Success(expenses)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getExpense(id: String): Result<ExpenseEntryDto> {
        return try {
            withContext(Dispatchers.IO) {
                val expense = postrest.from("expenses").select {
                    filter {
                        ExpenseEntryDto::id eq id
                    }
                }.decodeSingleOrNull<ExpenseEntryDto>()

                Result.Success(expense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun updateExpense(
        id: String,
        location: String?,
        distance: Float?,
        cost: Float?,
        locked: Boolean?
    ): Result<ExpenseEntryDto> {
        return try {

            val oldExpense = getExpense(id)
            if (oldExpense.data?.locked == true) {
                return Result.Error("Locked Expense cannot be updated")
            }

            withContext(Dispatchers.IO) {
                val updatedExpense = postrest.from("expenses").update({
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

                Result.Success(updatedExpense)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun lockExpense(id: String): Result<ExpenseEntryDto> {
        return try {
            updateExpense(id, locked = true)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }
}
