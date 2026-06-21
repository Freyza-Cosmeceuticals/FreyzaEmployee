package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.AuthState
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.auth.user.UserInfo

interface AuthenticationRepository {
    val authState: Flow<AuthState>
    suspend fun checkSession()
    suspend fun login(email: String, password: String): Result<UserInfo>
    suspend fun register(name: String, email: String, password: String): Result<UserInfo>
    suspend fun loginWithGoogle(): Result<UserInfo>
    suspend fun logout(): Result<Unit>
}
