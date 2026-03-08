package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateVisitUseCaseImpl(private val dailyReportRepository: DailyReportRepository) :
  CreateVisitUseCase {

  override suspend fun execute(input: CreateVisitUseCase.Input): CreateVisitUseCase.Output {

    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.createVisit(
        input.today,
        input.employeeId,
        input.dailyReportId,
        input.visitCreateDto
      )) {
        is Result.Success -> {
          CreateVisitUseCase.Output.Success(result.data)
        }

        else -> {
          CreateVisitUseCase.Output.Failure(result.message ?: "")
        }
      }
    }
  }
}
