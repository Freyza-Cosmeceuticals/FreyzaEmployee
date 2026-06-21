package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
  sessionManager: SessionManager,
  private val snackbarManager: SnackbarManager,
  serverTime: ServerTime,
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
    Logger.i(TAG, "Init")
  }
}
