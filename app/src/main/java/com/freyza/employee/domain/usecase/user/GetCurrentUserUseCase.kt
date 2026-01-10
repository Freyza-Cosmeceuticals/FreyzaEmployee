package com.freyza.employee.domain.usecase.user

import com.freyza.employee.domain.usecase.UseCase
import io.github.jan.supabase.auth.user.UserInfo

interface GetCurrentUserUseCase : UseCase<GetCurrentUserUseCase.Input, GetCurrentUserUseCase.Output> {
    class Input()

    sealed class Output() {
        data class Success(val user: UserInfo?) : Output()
        data class Failure(val message: String) : Output()
    }
}
