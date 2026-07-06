package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.presentation.ui.state.ReportDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportDetailViewModel(
  reportId: String,
  private val sessionManager: SessionManager,
  private val dailyReportRepository: DailyReportRepository,
  private val getAllRoutesWithLocationUseCase: GetAllRoutesWithLocationUseCase,
  private val lockReportUseCase: LockReportUseCase,
  private val snackbarManager: SnackbarManager,
  serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "ReportDetailViewModel"
  }

  private val _uiState = MutableStateFlow(
    ReportDetailUiState(
      today = serverTime.nowLocalDateTime(), reportId = reportId
    )
  )
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    ReportDetailUiState(today = serverTime.nowLocalDateTime(), reportId = reportId)
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
  }

  fun refresh() {
    Logger.d(TAG, "Refreshing data")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {
      loadDailyReport(reportId = uiState.value.reportId)
      loadAllRoutes()
    }
  }

  fun loadDailyReport(reportId: String) {
    Logger.i(TAG, "Fetching daily report:$reportId")

    _uiState.update {
      it.copy(report = UIState.Loading(it.report.data, "Loading report"))
    }

    viewModelScope.launch {
      when (val result = dailyReportRepository.getDailyReport(id = reportId, withVisits = true)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(report = UIState.Ready(result.data))
          }
          Logger.d(
            TAG, "Daily Report Fetched Successfully id:${result.data?.id}"
          )
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(
              report = UIState.Error(message = "Unable to fetch daily report")
            )
          }
          Logger.e(TAG, "Cannot fetch daily report: ${result.message}")
        }

        else -> {}
      }
    }
  }

  fun lockReport(reportId: String) {
    Logger.i(TAG, "Locking report:$reportId")

    _uiState.update {
      it.copy(lockingState = UIState.Loading())
    }

    viewModelScope.launch {
      when (lockReportUseCase(reportId)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(lockingState = UIState.Ready(Unit, "Report locked"))
          }
          snackbarManager.showSuccess("Report locked successfully")
          Logger.d(TAG, "report:$reportId locked successfully")

          // refresh
          loadDailyReport(reportId = reportId)
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(lockingState = UIState.Error("Unable to lock, please try again"))
          }
          snackbarManager.showError("Failed to lock report, please try again")
          Logger.e(TAG, "Failed to lock report:$reportId")
        }

        else -> {}
      }
    }
  }

  private fun loadAllRoutes() {
    Logger.i(TAG, "Fetching all routes")

    viewModelScope.launch {
      when (val result = getAllRoutesWithLocationUseCase()) {
        is Result.Success -> {
          _uiState.update {
            it.copy(routes = result.data)
          }
          Logger.d(
            TAG, "All routes fetched successfully: ${result.data.size} routes"
          )
        }

        is Result.Error -> {
          Logger.e(TAG, "Cannot fetch all routes with location: ${result.message}")
        }

        else -> {}
      }
    }
  }
}
