package com.freyza.employee.domain.model

data class MainUiState(
    val hasValidSession: Boolean = false,
    val user: User? = null
)
