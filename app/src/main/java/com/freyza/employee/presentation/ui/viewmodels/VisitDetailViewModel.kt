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
import com.freyza.employee.presentation.ui.state.VisitDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VisitDetailViewModel(
  private val visitId: String,
  private val sessionManager: SessionManager,
  private val dailyReportRepository: DailyReportRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "VisitDetailViewModel"
  }

  private val _uiState = MutableStateFlow(VisitDetailUiState(visitId = visitId))
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5_000), VisitDetailUiState(visitId = visitId)
  )

  val currentUser = sessionManager.currentEmployee

  fun getToday() = serverTime.nowLocalDateTime()

  fun refresh() {
    loadVisit()
  }

  private fun loadVisit() {
    Logger.d(TAG, "Fetching visit: $visitId")
    _uiState.update { it.copy(visit = UIState.Loading(it.visit.data)) }

    viewModelScope.launch {
      when (val result = dailyReportRepository.getVisit(visitId)) {
        is Result.Success -> {
          _uiState.update { it.copy(visit = UIState.Ready(result.data)) }
          result.data?.reportId?.let { loadReport(it) }
        }

        is Result.Error -> {
          _uiState.update { it.copy(visit = UIState.Error(result.message)) }
          Logger.e(TAG, "Error fetching visit: ${result.message}")
        }

        else -> {}
      }
    }
  }

  private fun loadReport(reportId: String) {
    _uiState.update { it.copy(report = UIState.Loading()) }
    viewModelScope.launch {
      when (val result = dailyReportRepository.getDailyReport(reportId)) {
        is Result.Success -> {
          _uiState.update { it.copy(report = UIState.Ready(result.data)) }
        }

        is Result.Error -> {
          _uiState.update { it.copy(report = UIState.Error(result.message)) }
        }

        else -> {}
      }
    }
  }

  fun deleteVisit() {
    Logger.i(TAG, "Deleting visit: $visitId")
    _uiState.update { it.copy(deletingState = UIState.Loading()) }

    viewModelScope.launch {
      when (val result = dailyReportRepository.deleteVisit(visitId)) {
        is Result.Success -> {
          _uiState.update { it.copy(deletingState = UIState.Ready(Unit)) }
          snackbarManager.showSuccess("Visit deleted successfully")
        }

        is Result.Error -> {
          _uiState.update { it.copy(deletingState = UIState.Error(result.message)) }
          snackbarManager.showError("Failed to delete visit")
          Logger.e(TAG, "Error deleting visit: ${result.message}")
        }

        else -> {}
      }
    }
  }
}
