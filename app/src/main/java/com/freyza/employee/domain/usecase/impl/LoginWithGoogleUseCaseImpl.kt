package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.common.AuthResponse
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginWithGoogleUseCaseImpl(private val authRepository: AuthenticationRepository) :
    LoginWithGoogleUseCase {
    override suspend fun execute(input: LoginWithGoogleUseCase.Input): LoginWithGoogleUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.loginWithGoogle()
            when (result) {
                is AuthResponse.Success -> {
                    LoginWithGoogleUseCase.Output.Success
                }

                is AuthResponse.Error -> {
                    LoginWithGoogleUseCase.Output.Failure(result.message)
                }
            }
        }
    }
}
