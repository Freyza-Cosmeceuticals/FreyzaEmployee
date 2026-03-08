package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetRecentDailyReportsUseCaseImpl(private val dailyReportRepository: DailyReportRepository): GetRecentDailyReportsUseCase {
  override suspend fun execute(input: GetRecentDailyReportsUseCase.Input): GetRecentDailyReportsUseCase.Output {
    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.getRecentDailyReports(input.numDailyReports, input.employeeId, input.withVisits)) {

        is Result.Success -> GetRecentDailyReportsUseCase.Output.Success(result.data ?: listOf())
        is Result.Error -> GetRecentDailyReportsUseCase.Output.Failure(result.message ?: "")
        else -> GetRecentDailyReportsUseCase.Output.Failure(result.message ?: "")
      }
    }
  }
}
