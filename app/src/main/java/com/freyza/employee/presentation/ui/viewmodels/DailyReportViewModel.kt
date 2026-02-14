package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase

class DailyReportViewModel(
  private val sessionManager: SessionManager,
  private val getTodayDailyReportUseCase: GetTodayDailyReportUseCase,
  private val createTodayDailyReportUseCase: GetTodayDailyReportUseCase,
) : ViewModel() {

  companion object {
    const val TAG = "DailyReportViewModel"
  }
}
