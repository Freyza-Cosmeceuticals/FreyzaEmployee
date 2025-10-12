package com.freyza.employee.domain.model

data class Expense(
    val id: String,
    val location: String,
    val distance: Double,
    val cost: Double,
    val locked: Boolean = false
)

fun dummyExpense(): Expense =
    Expense("670a2a4a-95c2-401c-8a2a-4a95c2801c45", "Darbhanga", 40.0, 200.0, false)

fun dummyExpenses(): List<Expense> = listOf(
    dummyExpense(),
    Expense("17f89e60-e4c8-408a-b89e-60e4c8008ae5", "Katihar", 80.0, 500.0, true)
)
