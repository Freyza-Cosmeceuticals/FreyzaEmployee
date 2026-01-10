package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.auth.LogoutUseCase.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoutUseCaseImpl(private val authRepository: AuthenticationRepository) : LogoutUseCase {
    override suspend fun execute(input: LogoutUseCase.Input): LogoutUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.logout()

            when (result) {
                is AuthResponse.Success -> Output.Success
                is AuthResponse.Error -> Output.Failure(result.message)
                AuthResponse.Logout -> Output.Success
            }
        }
    }
}
