package com.freyza.employee.domain.usecase

interface LogoutUseCase : UseCase<LogoutUseCase.Input, LogoutUseCase.Output> {
    class Input
    sealed class Output() {
        object Success : Output()
        object Failure : Output()
    }
}
