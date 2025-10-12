package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.domain.usecase.UseCase
import io.github.jan.supabase.auth.user.UserInfo

interface LogoutUseCase : UseCase<LogoutUseCase.Input, LogoutUseCase.Output> {
    class Input
    sealed class Output() {
        data class Success(val userInfo: UserInfo? = null) : Output()
        data class Failure(val message: String) : Output()
    }
}
