package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.usecase.UseCase
import kotlinx.datetime.LocalDate

interface CreateVisitUseCase : UseCase<CreateVisitUseCase.Input, CreateVisitUseCase.Output> {
  class Input(
    val today: LocalDate,
    val employeeId: String,
    val dailyReportId: String,
    val visitCreateDto: VisitCreateDto,
  )

  sealed class Output() {
    data class Success(val visit: Visit?) : Output()
    data class Failure(val message: String) : Output()
  }
}
