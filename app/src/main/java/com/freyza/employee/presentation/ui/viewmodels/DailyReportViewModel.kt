package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.dailyreport.GetAllDailyReportsUseCase
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
  private val getAllDailyReportUseCase: GetAllDailyReportsUseCase,
  private val getAllRoutesWithLocationUseCase: GetAllRoutesWithLocationUseCase,
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
    Logger.i(TAG, "Fetching all daily reports for emp:$employeeId")

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
        GetAllDailyReportsUseCase.Input(
          Constants.NUM_RECENT_DAILY_REPORTS,
          employeeId
        )
      )) {
        is GetAllDailyReportsUseCase.Output.Success -> {
          _uiState.update {
            it.copy(dailyReports = UIState.Ready(result.dailyReports))
          }
          Logger.d(
            TAG, "${result.dailyReports.size} Daily Reports Fetched Successfully"
          )
        }

        is GetAllDailyReportsUseCase.Output.Failure -> {
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
