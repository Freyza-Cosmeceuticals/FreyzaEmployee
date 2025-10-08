package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.common.AuthResponse
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterUseCaseImpl(private val authRepository: AuthenticationRepository) : RegisterUseCase {
    override suspend fun execute(input: RegisterUseCase.Input): RegisterUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.register(input.name, input.email, input.password)
            when (result) {
                is AuthResponse.Success -> {
                    RegisterUseCase.Output.Success
                }

                is AuthResponse.Error -> {
                    RegisterUseCase.Output.Failure(result.message)
                }
            }
        }
    }
}
