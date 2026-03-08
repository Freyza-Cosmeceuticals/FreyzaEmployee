package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.domain.usecase.UseCase

interface LockReportUseCase :
  UseCase<LockReportUseCase.Input, LockReportUseCase.Output> {
  class Input(val reportId: String)

  sealed class Output() {
    data class Success(val locked: Boolean) : Output()
    data class Failure(val message: String) : Output()
  }
}
