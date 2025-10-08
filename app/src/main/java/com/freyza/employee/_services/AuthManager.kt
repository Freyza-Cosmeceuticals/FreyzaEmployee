package com.freyza.employee._services

import com.freyza.employee.util._AuthResponse
import kotlinx.coroutines.flow.Flow

interface AuthManager {
    suspend fun loginWithEmail(emailValue: String, passwordValue: String): Flow<_AuthResponse>
    suspend fun loginWithGoogle(): Flow<_AuthResponse>
    suspend fun logout(): Flow<_AuthResponse>
    suspend fun isLoggedIn(): Flow<_AuthResponse>
}
