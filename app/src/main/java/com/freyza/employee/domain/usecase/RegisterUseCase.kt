package com.freyza.employee.domain.usecase

interface RegisterUseCase : UseCase<RegisterUseCase.Input, RegisterUseCase.Output> {
    class Input(val name: String, val email: String, val password: String)
    sealed class Output {
        object Success : Output()
        data class Failure(val message: String) : Output()
    }
}
