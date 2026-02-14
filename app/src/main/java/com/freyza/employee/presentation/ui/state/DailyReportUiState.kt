package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.Visit

data class DailyReportUiState(
  val currentDailyReport: UIState<DailyReport?> = UIState.Idle(),
  val visits: UIState<List<Visit>> = UIState.Idle(),
)
