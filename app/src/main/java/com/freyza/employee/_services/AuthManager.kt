package com.freyza.employee._services

import com.freyza.employee.common.AuthResponse
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    suspend fun loginWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse>
    suspend fun loginWithGoogle(): Flow<AuthResponse>
    suspend fun logout(): Flow<AuthResponse>
    suspend fun isLoggedIn(): Flow<AuthResponse>
}
