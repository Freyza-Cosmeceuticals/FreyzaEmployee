package com.freyza.employee.domain.usecase

interface LoginWithGoogleUseCase :
    UseCase<LoginWithGoogleUseCase.Input, LoginWithGoogleUseCase.Output> {
    class Input
    sealed class Output() {
        object Success : Output()
        object Failure : Output()
    }
}
