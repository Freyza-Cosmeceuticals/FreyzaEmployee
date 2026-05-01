package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.usecase.auth.LoginParams
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
  private val loginUseCase: LoginUseCase,
  private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
  private val snackbarManager: SnackbarManager,
) : ViewModel() {

  companion object {
    const val TAG = "LoginViewModel"
  }

  private val _uiState = MutableStateFlow<UIState<UserInfo>>(UIState.Idle())
  val uiState = _uiState.asStateFlow()

  init {
    Logger.d(TAG, "Init")
  }

  fun loginWithEmail(email: String, password: String) {
    if (email.isBlank() || password.isBlank()) {
      _uiState.update {
        // TODO: Replace with R.string.error_blank_email_pass
        UIState.Error("Please enter email and password", null)
      }
      snackbarManager.showError("Please enter email and password")
      return
    }

    _uiState.value = UIState.Loading()
    viewModelScope.launch {
      when (val result = loginUseCase(LoginParams(email.trim(), password.trim()))) {
        is Result.Success -> {
          _uiState.update {
            UIState.Ready(result.data)
          }
          Logger.d(TAG, "Login with email success")
        }

        is Result.Error -> {
          _uiState.update {
            UIState.Error(result.message, null)
          }
          snackbarManager.showError("Login failed, please try again")
          Logger.d(TAG, "Login with email failed ${result.message}")
        }

        is Result.Loading -> {
          _uiState.update {
            UIState.Loading()
          }
        }
      }
    }
  }

  fun loginWithGoogle() {
    _uiState.value = UIState.Loading()

    viewModelScope.launch {
      when (val result = loginWithGoogleUseCase()) {
        is Result.Success -> {
          _uiState.update {
            UIState.Ready(result.data)
          }
          Logger.d(TAG, "Login with google success")
        }

        is Result.Error -> {
          _uiState.update {
            UIState.Error("Login failed ${result.message}", null)
          }
          snackbarManager.showError("Login failed, please try again")
          Logger.d(TAG, "Login with google failed ${result.message}")
        }

        is Result.Loading -> {
          _uiState.update {
            UIState.Loading()
          }
        }
      }
    }
  }
}
