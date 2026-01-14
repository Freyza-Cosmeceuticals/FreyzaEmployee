package com.freyza.employee.domain.usecase.location

import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.usecase.UseCase

interface GetLocationUseCase : UseCase<GetLocationUseCase.Input, GetLocationUseCase.Output> {
    class Input(val id: String)

    sealed class Output() {
        data class Success(val location: Location?) : Output()
        data class Failure(val message: String) : Output()
    }
}
