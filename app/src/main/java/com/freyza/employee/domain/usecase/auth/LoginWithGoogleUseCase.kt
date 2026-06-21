package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.user.UserInfo

class LoginWithGoogleUseCase(private val authRepository: AuthenticationRepository) {
  suspend operator fun invoke(): Result<UserInfo> {
    return authRepository.loginWithGoogle()
  }
}
