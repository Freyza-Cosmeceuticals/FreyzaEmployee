package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.domain.usecase.UseCase

interface LogoutUseCase : UseCase<LogoutUseCase.Input, LogoutUseCase.Output> {
    class Input
    sealed class Output() {
        object Success : Output()
        data class Failure(val message: String) : Output()
    }
}
