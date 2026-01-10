package com.freyza.employee.domain.usecase.auth.impl

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginWithGoogleUseCaseImpl(private val authRepository: AuthenticationRepository) :
    LoginWithGoogleUseCase {
    override suspend fun execute(input: LoginWithGoogleUseCase.Input): Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.loginWithGoogle()
            when (result) {
                is AuthResponse.Success -> {
                    Output.Success(result.userInfo)
                }

                is AuthResponse.Error -> {
                    Output.Failure(result.message)
                }

                AuthResponse.Logout -> {
                    Output.Logout
                }
            }
        }
    }
}
