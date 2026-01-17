package com.freyza.employee.domain.usecase.user.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetUserUseCaseImpl(val userRepository: UserRepository) : GetUserUseCase {
    override suspend fun execute(input: GetUserUseCase.Input): GetUserUseCase.Output {
        return withContext(Dispatchers.IO) {
            when (val result = userRepository.getUserById(input.id)) {
                is Result.Success -> {
                    GetUserUseCase.Output.Success(result.data)
                }

                is Result.Error -> {
                    GetUserUseCase.Output.Failure(result.message ?: "")
                }

                else -> {
                    GetUserUseCase.Output.Failure(result.message ?: "")
                }
            }
        }
    }
}
