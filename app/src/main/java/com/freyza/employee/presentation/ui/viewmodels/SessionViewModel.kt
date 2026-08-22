package com.freyza.employee.presentation.ui.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.AuthState
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.sentry.Breadcrumb
import io.sentry.Sentry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    Sentry.addBreadcrumb(Breadcrumb().apply {
      category = "ui.action"
      message = "SessionViewModel.checkAuth triggered"
    })
    Logger.d(TAG, "Checking Auth session")
    viewModelScope.launch {
      authRepository.checkSession()
    }
  }

  fun syncTime() {
    Sentry.addBreadcrumb(Breadcrumb().apply {
      category = "ui.action"
      message = "SessionViewModel.syncTime triggered"
    })
    viewModelScope.launch {
      serverTime.syncTime()
    }
  }

  fun getTodayDateFormatted(): String {
    return DateFormatter.format(serverTime.nowLocalDateTime())
  }

  fun logout() {
    Sentry.addBreadcrumb(Breadcrumb().apply {
      category = "ui.action"
      message = "SessionViewModel.logout triggered"
    })
    Logger.d(TAG, "Logging out...")
    viewModelScope.launch {
      authRepository.logout()
    }
  }

  fun exit(context: Context) {
    Logger.d(TAG, "Exiting...")
    (context as? Activity)?.finishAffinity()
  }
}
