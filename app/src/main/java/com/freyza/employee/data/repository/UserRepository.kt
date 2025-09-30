package com.freyza.employee.data.repository

import com.freyza.employee.data.models.User
import com.freyza.employee.util.Result

interface UserRepository {
    suspend fun login(username: String, password: String): Result<User>
}