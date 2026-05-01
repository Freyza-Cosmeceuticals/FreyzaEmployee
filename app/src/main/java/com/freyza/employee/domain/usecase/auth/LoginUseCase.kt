package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.user.UserInfo

data class LoginParams(val email: String, val password: String)

class LoginUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(params: LoginParams): Result<UserInfo> {
    return when (val result = authRepository.login(params.email, params.password)) {
      is AuthResponse.Success -> Result.Success(result.userInfo)
      is AuthResponse.Error -> Result.Error(result.message)
      is AuthResponse.Logout -> Result.Error("User logged out")
    }
  }
}
