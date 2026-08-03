package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
  private val auth: Auth,
  private val postgres: Postgrest,
) : UserRepository {
  companion object {
    const val TAG: String = "UserRepository"
  }

  private val cachedHqEmployees = mutableMapOf<String, List<User>>()

  override suspend fun getUserById(id: String): Result<User?> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        val userDto = postgres.from("user").select {
          filter {
            UserDto::id eq id
          }
        }.decodeSingleOrNull<UserDto>()

        userDto?.toDomain()
      }
    }
  }

  override suspend fun getCurrentUser(): Result<UserInfo?> {
    return safeApiCall(TAG) {
      val userInfo = auth.currentUserOrNull()
      userInfo
    }
  }

  override suspend fun getEmployeesByHq(hqId: String, forceRefresh: Boolean): Result<List<User>> {
    return safeApiCall(TAG) {
      if (!forceRefresh && cachedHqEmployees.containsKey(hqId)) {
        Logger.d(TAG, "Returning cached employees for HQ: $hqId")

        return@safeApiCall cachedHqEmployees[hqId]!!
      }

      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying employees by hq:${hqId} from network")

        val usersDto = postgres.from("user").select {
          filter {
            UserDto::hqId eq hqId
            UserDto::status eq UserStatus.ACTIVE
            UserDto::role eq UserRole.EMPLOYEE
          }
        }.decodeList<UserDto>()

        val employees = usersDto.map { it.toDomain() }
        cachedHqEmployees[hqId] = employees

        employees
      }
    }
  }
}
