package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
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

  override suspend fun getUserById(id: String): Result<User?> {
    return try {
      withContext(Dispatchers.IO) {
        val userDto = postgres.from("user").select {
          filter {
            UserDto::id eq id
          }
        }.decodeSingleOrNull<UserDto>()

        Result.Success(userDto?.toDomain())
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getCurrentUser(): Result<UserInfo?> {
    return try {
      val userInfo = auth.currentUserOrNull()
      Result.Success(userInfo)
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }

  override suspend fun getEmployeesByHq(hqId: String): Result<List<User>> {
    return try {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Querying employees by hq:${hqId}")

        val usersDto = postgres.from("user").select {
          filter {
            UserDto::hqId eq hqId
            UserDto::status eq "ACTIVE"
            UserDto::role eq "EMPLOYEE"
          }
        }.decodeList<UserDto>()

        Result.Success(usersDto.map { it.toDomain() })
      }
    } catch (e: Exception) {
      Logger.e(TAG, e.message.toString())
      Result.Error(e.message.toString())
    }
  }
}
