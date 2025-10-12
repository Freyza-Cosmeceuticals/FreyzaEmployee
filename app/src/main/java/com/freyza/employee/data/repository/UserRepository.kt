package com.freyza.employee.data.repository

import com.freyza.employee.common.Result
import com.freyza.employee.domain.model.User

interface UserRepository {
    suspend fun getUserById(id: String): Result<User>
}
