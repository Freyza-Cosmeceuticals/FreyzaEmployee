package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {
  companion object {
    const val TAG = "PROFILE_VIEWMODEL"
  }

  private val _uiState = MutableStateFlow(ProfileScreenUiState())

  val uiState = _uiState
    .onStart {
      val employee = sessionManager.currentEmployee.value
      if (employee != null) _uiState.update { it.copy(user = UIState.Ready(employee)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileScreenUiState())
}
