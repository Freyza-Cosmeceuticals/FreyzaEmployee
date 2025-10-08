package com.freyza.employee.common

sealed interface AuthResponse {
    object Success : AuthResponse
    data class Error(val message: String) : AuthResponse
}
