package com.freyza.employee.domain.usecase.auth

import com.freyza.employee.domain.usecase.UseCase
import io.github.jan.supabase.auth.user.UserInfo

interface LoginWithGoogleUseCase :
    UseCase<LoginWithGoogleUseCase.Input, LoginWithGoogleUseCase.Output> {
    class Input
    sealed class Output() {
        data class Success(val userInfo: UserInfo? = null) : Output()
        data class Failure(val message: String) : Output()
    }
}
