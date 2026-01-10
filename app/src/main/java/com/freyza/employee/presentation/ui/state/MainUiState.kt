package com.freyza.employee.presentation.ui.state

import com.freyza.employee.domain.model.User

data class MainUiState(
    val hasValidSession: Boolean = false,
    val user: User? = null
)
