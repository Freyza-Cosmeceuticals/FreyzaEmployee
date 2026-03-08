package com.freyza.employee.domain.usecase.dailyreport.impl

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LockReportUseCaseImpl(private val dailyReportRepository: DailyReportRepository) :
  LockReportUseCase {

  override suspend fun execute(input: LockReportUseCase.Input): LockReportUseCase.Output {

    return withContext(Dispatchers.IO) {
      when (val result = dailyReportRepository.lockReport(input.reportId)) {
        is Result.Success -> {
          LockReportUseCase.Output.Success(result.data == true)
        }

        else -> {
          LockReportUseCase.Output.Failure(result.message ?: "")
        }
      }
    }

  }
}
