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
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
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
  private val routeRepository: RouteRepository,
  private val userRepository: UserRepository,
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

  fun refresh(forceRefresh: Boolean = false) {
    Logger.d(TAG, "Refreshing data, forceRefresh:$forceRefresh")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {
      loadDailyReport(reportId = uiState.value.reportId, forceRefresh)
      loadAllRoutes(forceRefresh)
      loadHqEmployees(forceRefresh)
    }
  }

  fun loadDailyReport(reportId: String, forceRefresh: Boolean = false) {
    Logger.d(TAG, "Fetching daily report:$reportId")

    _uiState.update {
      it.copy(report = UIState.Loading(it.report.data, "Loading report"))
    }

    viewModelScope.launch {
      when (val result = dailyReportRepository.getDailyReport(
        id = reportId,
        withVisits = true,
        forceRefresh = forceRefresh
      )) {
        is Result.Success -> {
          _uiState.update {
            it.copy(report = UIState.Ready(result.data))
          }
          Logger.d(
            TAG, "Daily Report Fetched Successfully id:${result.data?.id}"
          )

          // Trigger POI fetch as soon as we have the routeId/destLocId
          result.data?.routeId?.let { loadRouteAndPois(it, forceRefresh) }
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
    Logger.d(TAG, "Locking report:$reportId")

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

  private fun loadRouteAndPois(routeId: String, forceRefresh: Boolean) {
    viewModelScope.launch {
      when (val result = routeRepository.getRoute(routeId, forceRefresh)) {
        is Result.Success -> {
          val destLocId = result.data?.destLocId
          if (destLocId != null) {
            loadPois(destLocId, forceRefresh)
          }
        }

        is Result.Error -> Logger.e(TAG, "Failed to load route: ${result.message}")
        else -> {}
      }
    }
  }

  private fun loadPois(locationId: String, forceRefresh: Boolean) {
    viewModelScope.launch {
      when (val result = dailyReportRepository.getPoisByLocation(locationId, forceRefresh)) {
        is Result.Success -> {
          _uiState.update { it.copy(pois = result.data) }
        }

        is Result.Error -> Logger.e(TAG, "Failed to load POIs: ${result.message}")
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
