package com.freyza.employee.presentation.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.usecase.LoginUseCase
import com.freyza.employee.domain.usecase.LoginWithGoogleUseCase
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
                    Log.d("AUTH", "Login with email success")
                }

                else -> {
                    _uiState.value = Result.Error("Error logging in")
                    Log.d("AUTH", "Login with email failed")

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
                    Log.d("AUTH", "Login with google success")
                }

                else -> {
                    _uiState.value = Result.Error("Login  failed")
                    Log.d("AUTH", "Login with google failed")
                }
            }
        }
    }
}
