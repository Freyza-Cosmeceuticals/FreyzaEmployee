package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
  private val sessionManager: SessionManager,
  private val locationRepository: LocationRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
) : ViewModel() {
  companion object {
    const val TAG = "ProfileViewModel"
  }

  private val _uiState =
    MutableStateFlow(ProfileScreenUiState(today = serverTime.nowLocalDateTime()))

  val uiState = _uiState
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5_000),
      ProfileScreenUiState(today = serverTime.nowLocalDateTime())
    )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
    loadHqName()
  }

  private fun loadHqName() {
    val hqId = sessionManager.currentEmployee.value?.hqId ?: return
    viewModelScope.launch {
      when (val result = locationRepository.getLocation(hqId)) {
        is Result.Success -> {
          _uiState.update { it.copy(hqName = result.data?.name) }
        }

        is Result.Error -> {
          Logger.e(TAG, "Error loading HQ name", Error(result.message))
        }
        else -> {}
      }
    }
  }
}
