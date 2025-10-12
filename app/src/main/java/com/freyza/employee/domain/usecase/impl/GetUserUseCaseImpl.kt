package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.domain.usecase.GetUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetUserUseCaseImpl(val userRepository: UserRepository) : GetUserUseCase {
    override suspend fun execute(input: GetUserUseCase.Input): GetUserUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = userRepository.getUserById(input.id)
            when (result) {
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
