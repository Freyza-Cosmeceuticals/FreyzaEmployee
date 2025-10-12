package com.freyza.employee.domain.usecase

import com.freyza.employee.data.network.dto.UserDto

interface GetUserUseCase : UseCase<GetUserUseCase.Input, GetUserUseCase.Output> {
    class Input(val id: String)

    sealed class Output() {
        data class Success(val user: UserDto?) : Output()
        data class Failure(val message: String) : Output()
    }
}
