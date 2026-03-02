package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateTodayDailyReportUseCaseImpl(private val dailyReportRepository: DailyReportRepository) :
  CreateTodayDailyReportUseCase {

  override suspend fun execute(input: CreateTodayDailyReportUseCase.Input): CreateTodayDailyReportUseCase.Output {
    
    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.createTodayDailyReport(
        input.today,
        input.employeeId,
        input.dayType,
        input.routeId
      )) {
        is Result.Success -> {
          CreateTodayDailyReportUseCase.Output.Success(result.data)
        }

        is Result.Error -> {
          CreateTodayDailyReportUseCase.Output.Failure(result.message ?: "")
        }

        is Result.Loading -> {
          CreateTodayDailyReportUseCase.Output.Failure(result.message ?: "")
        }
      }
    }
  }
}
