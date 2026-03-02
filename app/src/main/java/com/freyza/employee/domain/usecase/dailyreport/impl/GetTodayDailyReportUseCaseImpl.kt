package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetTodayDailyReportUseCaseImpl(private val dailyReportRepository: DailyReportRepository) :
  GetTodayDailyReportUseCase {

  override suspend fun execute(input: GetTodayDailyReportUseCase.Input): GetTodayDailyReportUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.getTodayDailyReport(input.today, input.employeeId)) {
        is Result.Success -> {
          GetTodayDailyReportUseCase.Output.Success(result.data)
        }

        is Result.Error -> {
          GetTodayDailyReportUseCase.Output.Failure(result.message ?: "")
        }

        is Result.Loading -> {
          GetTodayDailyReportUseCase.Output.Failure(result.message ?: "")
        }
      }
    }
  }
}
