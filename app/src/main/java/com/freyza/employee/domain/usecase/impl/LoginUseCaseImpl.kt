package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginUseCaseImpl(private val authRepository: AuthenticationRepository) : LoginUseCase {
    override suspend fun execute(input: LoginUseCase.Input): LoginUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.login(input.email, input.password)
            if (result) {
                LoginUseCase.Output.Success
            } else {
                LoginUseCase.Output.Failure
            }
        }
    }
}
