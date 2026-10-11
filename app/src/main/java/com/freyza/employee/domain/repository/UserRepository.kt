package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.User
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
  suspend fun getUserById(id: String): Result<User?>
  suspend fun getCurrentUser(): Result<UserInfo?>
  suspend fun getAllEmployees(forceRefresh: Boolean = false): Result<List<User>>
  fun observeAllEmployees(): Flow<List<User>>
  suspend fun refreshEmployees(): Result<Unit>
  fun clearCache()
}
