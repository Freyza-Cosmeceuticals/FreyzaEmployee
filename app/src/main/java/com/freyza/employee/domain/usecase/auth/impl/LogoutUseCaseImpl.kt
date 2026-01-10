package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoutUseCaseImpl(private val authRepository: AuthenticationRepository) : LogoutUseCase {
    override suspend fun execute(input: LogoutUseCase.Input): LogoutUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.logout()

            when (result) {
                is AuthResponse.Success -> {
                    LogoutUseCase.Output.Success()
                }

                is AuthResponse.Error -> {
                    LogoutUseCase.Output.Failure(result.message)
                }
            }
        }
    }
}
