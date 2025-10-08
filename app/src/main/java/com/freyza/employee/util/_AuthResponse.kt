package com.freyza.employee.util

import com.freyza.employee.data.network.dto.UserDto

sealed interface _AuthResponse {
    data class Success(val user: UserDto?) : _AuthResponse
    data class Error(val message: String) : _AuthResponse
}
