package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
) : ViewModel() {

    companion object {
        const val TAG = "AUTH"
    }

    private val _uiState = MutableStateFlow<Result<UserInfo>?>(null)
    val uiState: StateFlow<Result<UserInfo>?> = _uiState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        _uiState.value = Result.Loading()

        viewModelScope.launch {
            val result = loginUseCase.execute(LoginUseCase.Input(email, password))

            when (result) {
                is LoginUseCase.Output.Success -> {
                    _uiState.update { Result.Success(result.userInfo) }
                    Logger.d(TAG, "Login with email success")
                }

                is LoginUseCase.Output.Failure -> {
                    _uiState.update {
                        Result.Error(
                            result.message,
                            data = null
                        )
                    }
                    Logger.d(TAG, "Login with email failed ${result.message}")
                }
            }
        }
    }

    fun loginWithGoogle() {
        _uiState.value = Result.Loading()

        viewModelScope.launch {
            val result = loginWithGoogleUseCase.execute(LoginWithGoogleUseCase.Input())

            when (result) {
                is LoginWithGoogleUseCase.Output.Success -> {
                    _uiState.value = Result.Success(result.userInfo)
                    Logger.d(TAG, "Login with google success")
                }

                is LoginWithGoogleUseCase.Output.Failure -> {
                    _uiState.value = Result.Error("Login  failed ${result.message}")
                    Logger.d(TAG, "Login with google failed ${result.message}")
                }
            }
        }
    }
}
