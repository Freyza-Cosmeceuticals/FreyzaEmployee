package com.freyza.employee.data.repository

import com.freyza.employee.common.AuthResponse
import com.freyza.employee.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationRepository {
    val authState: StateFlow<AuthState>
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun register(name: String, email: String, password: String): AuthResponse
    suspend fun loginWithGoogle(): AuthResponse
    suspend fun exchangeCodeForSession(code: String): Result<Unit>
    suspend fun verifyEmail(tokenHash: String): Result<Unit>
    suspend fun logout(): AuthResponse
}
