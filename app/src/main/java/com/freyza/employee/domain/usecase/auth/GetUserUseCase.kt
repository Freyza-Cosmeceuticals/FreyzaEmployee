package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.usecase.UseCase

interface GetUserUseCase : UseCase<GetUserUseCase.Input, GetUserUseCase.Output> {
    class Input(val id: String)

    sealed class Output() {
        data class Success(val user: User?) : Output()
        data class Failure(val message: String) : Output()
    }
}
