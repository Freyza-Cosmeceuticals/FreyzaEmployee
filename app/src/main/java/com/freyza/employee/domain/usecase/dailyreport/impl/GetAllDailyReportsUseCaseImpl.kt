package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.GetAllDailyReportsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetAllDailyReportsUseCaseImpl(private val dailyReportRepository: DailyReportRepository): GetAllDailyReportsUseCase {
  override suspend fun execute(input: GetAllDailyReportsUseCase.Input): GetAllDailyReportsUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.getRecentDailyReports(input.numDailyReports, input.employeeId)) {

        is Result.Success -> GetAllDailyReportsUseCase.Output.Success(result.data ?: listOf())
        is Result.Error -> GetAllDailyReportsUseCase.Output.Failure(result.message ?: "")
        else -> GetAllDailyReportsUseCase.Output.Failure(result.message ?: "")
      }
    }
  }
}
