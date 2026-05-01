package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository

class LogoutUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(): Result<Unit> {
    return when (val result = authRepository.logout()) {
      is AuthResponse.Success -> Result.Success(Unit)
      is AuthResponse.Error -> Result.Error(result.message)
      AuthResponse.Logout -> Result.Success(Unit)
    }
  }
}
