package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.common.AuthResponse
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginWithGoogleUseCaseImpl(private val authRepository: AuthenticationRepository) :
    LoginWithGoogleUseCase {
    override suspend fun execute(input: LoginWithGoogleUseCase.Input): LoginWithGoogleUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.loginWithGoogle()
            when (result) {
                is AuthResponse.Success -> {
                    LoginWithGoogleUseCase.Output.Success(result.userInfo)
                }

                is AuthResponse.Error -> {
                    LoginWithGoogleUseCase.Output.Failure(result.message)
                }
            }
        }
    }
}
