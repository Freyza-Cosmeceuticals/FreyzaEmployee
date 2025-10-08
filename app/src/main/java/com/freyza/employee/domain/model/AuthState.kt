package com.freyza.employee.domain.model

sealed interface AuthState {
    data object Initializing : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}
