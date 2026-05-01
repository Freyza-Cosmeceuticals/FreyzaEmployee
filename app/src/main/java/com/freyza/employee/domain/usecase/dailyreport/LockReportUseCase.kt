package com.freyza.employee.domain.usecase.dailyreport

import com.freyza.employee.core.Result
import com.freyza.employee.domain.repository.DailyReportRepository

class LockReportUseCase(private val dailyReportRepository: DailyReportRepository) {
  suspend operator fun invoke(reportId: String): Result<Boolean> {
    return when (val result = dailyReportRepository.lockReport(reportId)) {
      is Result.Success -> Result.Success(result.data == true)
      else -> result
    }
  }
}
