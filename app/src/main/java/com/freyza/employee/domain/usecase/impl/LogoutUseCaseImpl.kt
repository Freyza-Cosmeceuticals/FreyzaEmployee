package com.freyza.employee.domain.usecase.impl

import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoutUseCaseImpl(private val authRepository: AuthenticationRepository) : LogoutUseCase {
    override suspend fun execute(input: LogoutUseCase.Input): LogoutUseCase.Output {
        return withContext(Dispatchers.IO) {
            val result = authRepository.logout()
            if (result)
                LogoutUseCase.Output.Success
            else
                LogoutUseCase.Output.Failure
        }
    }
}
