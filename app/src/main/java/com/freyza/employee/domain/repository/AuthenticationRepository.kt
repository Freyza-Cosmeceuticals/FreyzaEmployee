package com.freyza.employee.domain.repository

import com.freyza.employee.core.AuthResponse

interface AuthenticationRepository {
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun register(name: String, email: String, password: String): AuthResponse
    suspend fun loginWithGoogle(): AuthResponse
    suspend fun logout(): AuthResponse
}
