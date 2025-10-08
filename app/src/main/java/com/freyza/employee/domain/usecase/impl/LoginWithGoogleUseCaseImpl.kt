package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginWithGoogleUseCaseImpl(private val authRepository: AuthenticationRepository) :
    LoginWithGoogleUseCase {
    override suspend fun execute(input: LoginWithGoogleUseCase.Input): LoginWithGoogleUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.loginWithGoogle()
            if (result)
                LoginWithGoogleUseCase.Output.Success
            else
                LoginWithGoogleUseCase.Output.Failure
        }
    }
}
