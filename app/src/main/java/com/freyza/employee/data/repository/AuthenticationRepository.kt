package com.freyza.employee.data.repository

import com.freyza.employee.common.AuthResponse

interface AuthenticationRepository {
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun register(name: String, email: String, password: String): AuthResponse
    suspend fun loginWithGoogle(): AuthResponse
    suspend fun logout(): AuthResponse
}
