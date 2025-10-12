package com.freyza.employee.domain.usecase

import io.github.jan.supabase.auth.user.UserInfo

interface RegisterUseCase : UseCase<RegisterUseCase.Input, RegisterUseCase.Output> {
    class Input(val name: String, val email: String, val password: String)
    sealed class Output {
        data class Success(val userInfo: UserInfo? = null) : Output()
        data class Failure(val message: String) : Output()
    }
}
