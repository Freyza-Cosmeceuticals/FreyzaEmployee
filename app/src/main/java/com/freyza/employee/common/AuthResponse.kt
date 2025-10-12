package com.freyza.employee.common

import io.github.jan.supabase.auth.user.UserInfo

sealed interface AuthResponse {
    data class Success(val userInfo: UserInfo? = null) : AuthResponse
    data class Error(val message: String) : AuthResponse
}
