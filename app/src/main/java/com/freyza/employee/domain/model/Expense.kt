package com.freyza.employee.domain.model

data class Expense(
    val id: String,
    val location: String,
    val distance: Double,
    val cost: Double,
    val locked: Boolean = false
)
