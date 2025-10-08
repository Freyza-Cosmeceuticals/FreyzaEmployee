package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.usecase.LoginUseCase
import com.freyza.employee.domain.usecase.LoginWithGoogleUseCase
import com.freyza.employee.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<Result<UserDto>?>(null)
    val uiState: StateFlow<Result<UserDto>?> = _uiState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        _uiState.value = Result.Loading()

        viewModelScope.launch {
            val result = loginUseCase.execute(LoginUseCase.Input(email, password))

            when (result) {
                is LoginUseCase.Output.Success -> {
                    _uiState.value = Result.Success()
                    Logger.d("AUTH", "Login with email success")
                }

                is LoginUseCase.Output.Failure -> {
                    _uiState.value = Result.Error("Error logging in ${result.message}")
                    Logger.d("AUTH", "Login with email failed ${result.message}")

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
                    _uiState.value = Result.Success()
                    Logger.d("AUTH", "Login with google success")
                }

                is LoginWithGoogleUseCase.Output.Failure -> {
                    _uiState.value = Result.Error("Login  failed ${result.message}")
                    Logger.d("AUTH", "Login with google failed ${result.message}")
                }
            }
        }
    }
}
