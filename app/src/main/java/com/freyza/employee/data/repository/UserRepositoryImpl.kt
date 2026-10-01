package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.local.dao.UserDao
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toEntity
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeesResponse(
  @SerialName("success")
  val success: Boolean,
  @SerialName("data")
  val data: List<UserDto>,
)

class UserRepositoryImpl(
  private val auth: Auth,
  private val postgres: Postgrest,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val userDao: UserDao,
) : UserRepository {
  companion object {
    const val TAG: String = "UserRepository"
  }

  override fun clearCache() {
    throw NotImplementedError()
  }

  override fun observeAllEmployees(): Flow<List<User>> {
    return userDao.getAllEmployeesFlow().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  override suspend fun getUserById(id: String): Result<User?> {
    val cached = userDao.getEmployeeById(id)
    return if (cached != null) {
      Result.Success(cached.toDomain())
    } else {
      refreshEmployees()
      Result.Success(userDao.getEmployeeById(id)?.toDomain())
    }
  }

  override suspend fun getCurrentUser(): Result<UserInfo?> {
    return safeApiCall(TAG) {
      val userInfo = auth.currentUserOrNull()
      userInfo
    }
  }

  override suspend fun getAllEmployees(forceRefresh: Boolean): Result<List<User>> {
    val cached = userDao.getAllEmployees()
    return if (cached.isNotEmpty() && !forceRefresh) {
      Result.Success(cached.map { it.toDomain() })
    } else {
      refreshEmployees()
      Result.Success(userDao.getAllEmployees().map { it.toDomain() })
    }
  }

  override suspend fun refreshEmployees(): Result<Unit> {
    return safeApiCall(TAG, auth) { token ->
      withContext(Dispatchers.IO) {
        val response = httpClient.get("${appConfig.apiUrl}/api/employees") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val employeesResponse = response.body<EmployeesResponse>()
          if (employeesResponse.success) {
            val userEntities = employeesResponse.data.map { it.toDomain().toEntity() }
            userDao.upsertEmployees(userEntities)
          } else {
            throw Exception("Failed to fetch employees")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw Exception(error.message)
        }
      }
    }
  }
}
