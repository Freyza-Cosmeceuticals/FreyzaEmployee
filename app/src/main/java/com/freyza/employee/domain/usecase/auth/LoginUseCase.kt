package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.user.UserInfo

data class LoginParams(val email: String, val password: String)

class LoginUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(params: LoginParams): Result<UserInfo> {
    return authRepository.login(params.email, params.password)
  }
}
