package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsParams
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsUseCase
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
  private val routeRepository: RouteRepository,
  private val userRepository: UserRepository,
  serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "DailyReportViewModel"
  }

  private val _uiState = MutableStateFlow(DailyReportUiState(today = serverTime.nowLocalDateTime()))
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    DailyReportUiState(today = serverTime.nowLocalDateTime())
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
  }

  fun refresh(forceRefresh: Boolean = false) {
    Logger.d(TAG, "Refreshing data, forceRefresh:$forceRefresh")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {
      loadAllDailyReports(employeeId)
      loadAllRoutes(forceRefresh)
      loadHqEmployees(forceRefresh)
    }
  }

  fun loadAllDailyReports(employeeId: String? = sessionManager.currentEmployee.value?.id) {
    Logger.d(TAG, "Fetching all daily reports for emp:$employeeId with visits")

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
      val result = getAllDailyReportUseCase(
        GetRecentDailyReportsParams(
          numDailyReports = Constants.NUM_RECENT_DAILY_REPORTS,
          employeeId = employeeId,
          withVisits = true,
        )
      )

      when (result) {
        is Result.Success -> {
          _uiState.update {
            it.copy(dailyReports = UIState.Ready(result.data))
          }
          Logger.i(
            TAG, "${result.data.size} Daily Reports Fetched Successfully"
          )
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(
              dailyReports = UIState.Error(message = "Unable to fetch daily reports")
            )
          }
          Logger.e(TAG, "Cannot fetch daily reports: ${result.message}")
        }

        else -> {}
      }
    }
  }

  private fun loadAllRoutes(forceRefresh: Boolean = false) {
    Logger.d(TAG, "Fetching all routes")

    viewModelScope.launch {
      when (val result = routeRepository.getAllRoutesWithLocation(forceRefresh)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(routes = result.data)
          }
        }

        is Result.Error -> {
          Logger.e(TAG, "Cannot fetch all routes with location: ${result.message}")
        }

        else -> {}
      }
    }
  }

  private fun loadHqEmployees(forceRefresh: Boolean = false) {
    val user = sessionManager.currentEmployee.value
    val hqId = user?.hqId ?: return

    viewModelScope.launch {
      when (val result = userRepository.getAllEmployees(hqId, forceRefresh)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(employees = result.data)
          }
        }

        is Result.Error -> Logger.e(TAG, "Fetch employees failed: ${result.message}")
        else -> {}
      }
    }
  }
}
