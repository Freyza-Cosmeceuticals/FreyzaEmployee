package com.freyza.employee.domain.usecase.user.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentUserUseCaseImpl(val userRepository: UserRepository) : GetCurrentUserUseCase {
    override suspend fun execute(input: GetCurrentUserUseCase.Input): GetCurrentUserUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = userRepository.getCurrentUser()
            when (result) {
                is Result.Success -> {
                    GetCurrentUserUseCase.Output.Success(result.data)
                }

                is Result.Error -> {
                    GetCurrentUserUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetCurrentUserUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }
}
