package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.user.UserInfo

data class RegisterParams(val name: String, val email: String, val password: String)

class RegisterUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(params: RegisterParams): Result<UserInfo?> {
    return when (val result = authRepository.register(params.name, params.email, params.password)) {
      is AuthResponse.Success -> Result.Success(result.userInfo)
      is AuthResponse.Error -> Result.Error(result.message)
      AuthResponse.Logout -> Result.Error("User logged out")
    }
  }
}
