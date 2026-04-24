package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsUseCase
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.presentation.ui.state.DailyReportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyReportViewModel(
  private val sessionManager: SessionManager,
  private val getAllDailyReportUseCase: GetRecentDailyReportsUseCase,
  private val getAllRoutesWithLocationUseCase: GetAllRoutesWithLocationUseCase,
  private val lockReportUseCase: LockReportUseCase,
  private val snackbarManager: SnackbarManager,
) : ViewModel() {

  companion object {
    const val TAG = "DailyReportViewModel"
  }

  private val _uiState = MutableStateFlow(DailyReportUiState())
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyReportUiState())

  init {
    Logger.d(TAG, "Init")
  }

  fun refresh() {
    Logger.d(TAG, "Refreshing data")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {
      loadAllDailyReports(employeeId)
      loadAllRoutes()
    }
  }

  fun loadAllDailyReports(employeeId: String? = sessionManager.currentEmployee.value?.id) {
    Logger.i(TAG, "Fetching all daily reports for emp:$employeeId with visits")

    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set. cannot load daily reports, aborting")
      return
    }

    _uiState.update {
      it.copy(
        dailyReports = UIState.Loading(
          it.dailyReports.data, "Loading Daily Reports"
        )
      )
    }

    viewModelScope.launch {
      when (val result = getAllDailyReportUseCase.execute(
        GetRecentDailyReportsUseCase.Input(
          Constants.NUM_RECENT_DAILY_REPORTS,
          employeeId,
          true,
        )
      )) {
        is GetRecentDailyReportsUseCase.Output.Success -> {
          _uiState.update {
            it.copy(dailyReports = UIState.Ready(result.dailyReports))
          }
          Logger.d(
            TAG, "${result.dailyReports.size} Daily Reports Fetched Successfully"
          )
        }

        is GetRecentDailyReportsUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              dailyReports = UIState.Error(message = "Unable to fetch daily reports")
            )
          }
          Logger.e(TAG, "Cannot fetch daily reports: ${result.message}")
        }
      }
    }
  }

  fun lockReport(reportId: String) {
    Logger.i(TAG, "Locking report:$reportId")

    _uiState.update {
      it.copy(lockingState = UIState.Loading())
    }

    viewModelScope.launch {
      when (lockReportUseCase.execute(LockReportUseCase.Input(reportId))) {
        is LockReportUseCase.Output.Success -> {
          _uiState.update {
            it.copy(lockingState = UIState.Ready(Unit, "Report locked"))
          }
          snackbarManager.showSuccess("Report locked successfully")
          Logger.d(TAG, "report:$reportId locked successfully")

          // refresh
          loadAllDailyReports()
        }

        is LockReportUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(lockingState = UIState.Error("Unable to lock, please try again"))
          }
          snackbarManager.showError("Failed to lock report, please try again")
          Logger.e(TAG, "Failed to lock report:$reportId")
        }
      }
    }
  }

  private fun loadAllRoutes() {
    Logger.i(TAG, "Fetching all routes")

    _uiState.update {
      it.copy(routes = UIState.Loading(it.routes.data))
    }

    viewModelScope.launch {
      when (val result =
        getAllRoutesWithLocationUseCase.execute(GetAllRoutesWithLocationUseCase.Input())) {
        is GetAllRoutesWithLocationUseCase.Output.Success -> {
          _uiState.update {
            it.copy(routes = UIState.Ready(result.routes))
          }
          Logger.d(
            TAG, "All routes fetched successfully: ${result.routes.size} routes"
          )
        }

        is GetAllRoutesWithLocationUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(
              routes = UIState.Error(message = "Unable to routes")
            )
          }
          Logger.e(TAG, "Cannot fetch all routes with location: ${result.message}")
        }
      }
    }
  }
}
