package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.User
import io.github.jan.supabase.auth.user.UserInfo

interface UserRepository {
  suspend fun getUserById(id: String): Result<User?>
  suspend fun getCurrentUser(): Result<UserInfo?>
  suspend fun getEmployeesByHq(hqId: String, forceRefresh: Boolean = false): Result<List<User>>
}
