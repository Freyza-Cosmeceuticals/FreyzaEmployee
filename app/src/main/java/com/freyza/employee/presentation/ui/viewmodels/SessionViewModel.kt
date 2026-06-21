package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.AuthState
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.ServerTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.app.Activity
import android.content.Context

class SessionViewModel(
  private val authRepository: AuthenticationRepository,
  private val serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "SessionViewModel"
  }

  val authState: StateFlow<AuthState> = authRepository.authState
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

  fun checkAuth() {
    Logger.i(TAG, "Checking Auth session")
    viewModelScope.launch {
      serverTime.syncTime()
      authRepository.checkSession()
    }
  }

  fun getTodayDateFormatted(): String {
    return DateFormatter.format(serverTime.nowLocalDateTime())
  }

  fun logout() {
    Logger.i(TAG, "Logging out...")
    viewModelScope.launch {
      authRepository.logout()
    }
  }

  fun exit(context: Context) {
    Logger.i(TAG, "Exiting...")
    (context as? Activity)?.finishAffinity()
  }
}
