package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginUseCase.Input
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
  private val loginUseCase: LoginUseCase,
  private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
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
      return
    }

    _uiState.value = UIState.Loading()
    viewModelScope.launch {
      when (val result = loginUseCase.execute(Input(email.trim(), password.trim()))) {
        is LoginUseCase.Output.Success -> {
          _uiState.update {
            UIState.Ready(result.userInfo)
          }
          Logger.d(TAG, "Login with email success")
        }

        is LoginUseCase.Output.Failure -> {
          _uiState.update {
            UIState.Error(result.message, null)
          }
          Logger.d(TAG, "Login with email failed ${result.message}")
        }

        LoginUseCase.Output.Logout -> {
          _uiState.update {
            UIState.Idle()
          }
        }
      }
    }
  }

  fun loginWithGoogle() {
    _uiState.value = UIState.Loading()

    viewModelScope.launch {
      when (val result = loginWithGoogleUseCase.execute(LoginWithGoogleUseCase.Input())) {
        is LoginWithGoogleUseCase.Output.Success -> {
          _uiState.update {
            UIState.Ready(result.userInfo)
          }
          Logger.d(TAG, "Login with google success")
        }

        is LoginWithGoogleUseCase.Output.Failure -> {
          _uiState.update {
            UIState.Error("Login  failed ${result.message}", null)
          }
          Logger.d(TAG, "Login with google failed ${result.message}")
        }

        LoginWithGoogleUseCase.Output.Logout -> {
          _uiState.update {
            UIState.Idle()
          }
        }
      }
    }
  }
}
