package com.freyza.employee.data.repository

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
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
) : UserRepository {
  companion object {
    const val TAG: String = "UserRepository"
  }

  // Cache for employee lists keyed by HQ ID
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

  override suspend fun getAllEmployees(hqId: String, forceRefresh: Boolean): Result<List<User>> {
    if (!forceRefresh && cachedHqEmployees.containsKey(hqId)) {
      Logger.d(TAG, "Returning cached employees for HQ: $hqId")

      return Result.Success(cachedHqEmployees[hqId]!!)
    }

    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching employees from server API")

        val token = auth.currentAccessTokenOrNull()
          ?: throw IllegalStateException("No authentication token found")

        // The API accepts an optional 'hqId' query parameter to filter employees.
        // For now, we are fetching all employees.
        val response = httpClient.get("${appConfig.apiUrl}/api/employees") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val employeesResponse = response.body<EmployeesResponse>()
          if (employeesResponse.success) {
            val employees = employeesResponse.data.map { it.toDomain() }
            cachedHqEmployees[hqId] = employees
            employees
          } else {
            throw Exception("Failed to fetch employees: success flag is false")
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
