package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginUseCaseImpl(private val authRepository: AuthenticationRepository) : LoginUseCase {
    override suspend fun execute(input: LoginUseCase.Input): LoginUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.login(input.email, input.password)
            when (result) {
                is AuthResponse.Success -> {
                    LoginUseCase.Output.Success(result.userInfo)
                }

                is AuthResponse.Error -> {
                    LoginUseCase.Output.Failure(result.message)
                }

                is AuthResponse.Logout -> {
                    LoginUseCase.Output.Logout
                }
            }
        }
    }
}
