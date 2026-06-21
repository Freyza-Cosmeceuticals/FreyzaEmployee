package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository

class LogoutUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(): Result<Unit> {
    return authRepository.logout()
  }
}
