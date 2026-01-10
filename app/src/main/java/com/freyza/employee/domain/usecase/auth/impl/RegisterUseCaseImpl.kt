package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterUseCaseImpl(private val authRepository: AuthenticationRepository) : RegisterUseCase {
    override suspend fun execute(input: RegisterUseCase.Input): RegisterUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.register(input.name, input.email, input.password)
            when (result) {
                is AuthResponse.Success -> {
                    RegisterUseCase.Output.Success(result.userInfo)
                }

                is AuthResponse.Error -> {
                    RegisterUseCase.Output.Failure(result.message)
                }
            }
        }
    }
}
