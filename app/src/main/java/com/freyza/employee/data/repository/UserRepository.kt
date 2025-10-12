package com.freyza.employee.data.repository

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto

interface UserRepository {
    suspend fun getUserById(id: String): Result<UserDto>
}
