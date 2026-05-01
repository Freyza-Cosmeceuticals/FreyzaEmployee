package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.core.Result
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.repository.DailyReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

data class CreateVisitParams(
  val today: LocalDate,
  val employeeId: String,
  val dailyReportId: String,
  val visitCreateDto: VisitCreateDto,
)

class CreateVisitUseCase(private val dailyReportRepository: DailyReportRepository) {
  suspend operator fun invoke(params: CreateVisitParams): Result<Visit> {
    return withContext(Dispatchers.IO) {
      dailyReportRepository.createVisit(
        params.today,
        params.employeeId,
        params.dailyReportId,
        params.visitCreateDto
      )
    }
  }
}
