package com.freyza.employee.domain.usecase.user

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.auth.user.UserInfo

class GetCurrentUserUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(): Result<UserInfo?> {
    return userRepository.getCurrentUser()
  }
}
