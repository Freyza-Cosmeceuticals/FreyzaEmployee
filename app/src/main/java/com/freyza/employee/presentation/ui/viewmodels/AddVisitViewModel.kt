package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AddVisitViewModel(val visitType: VisitType, private val sessionManager: SessionManager) :
  ViewModel() {
  companion object {
    const val TAG = "AddVisitViewModel"
  }

  private val _uiState = MutableStateFlow(AddVisitUiState(visitType = visitType))
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5_000), AddVisitUiState(visitType = visitType)
  )

  init {
    Logger.d(TAG, "Init with visitType: ${visitType.name}")
  }

  fun refresh() {
    Logger.d(TAG, "Refreshing data")

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) {

    }
  }
}
