package com.freyza.employee.domain.usecase

import io.github.jan.supabase.auth.user.UserInfo

interface LoginUseCase : UseCase<LoginUseCase.Input, LoginUseCase.Output> {
    class Input(val email: String, val password: String)
    sealed class Output() {
        data class Success(val userInfo: UserInfo? = null) : Output()
        data class Failure(val message: String) : Output()
    }
}
