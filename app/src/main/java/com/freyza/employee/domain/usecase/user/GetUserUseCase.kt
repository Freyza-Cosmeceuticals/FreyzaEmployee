package com.freyza.employee.domain.usecase.user

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.repository.UserRepository

class GetUserUseCase(private val userRepository: UserRepository) {
  suspend operator fun invoke(id: String): Result<User> {
    return userRepository.getUserById(id)
  }
}
