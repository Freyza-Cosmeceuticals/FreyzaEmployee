package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Result
import com.freyza.employee.domain.usecase.LogoutUseCase
import com.freyza.employee.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val logoutUseCase: LogoutUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow<Result<Nothing>?>(null)
    val uiState = _uiState.asStateFlow()

    fun logout() {
        _uiState.value = Result.Loading()

        viewModelScope.launch {
            val result = logoutUseCase.execute(LogoutUseCase.Input())

            when (result) {
                is LogoutUseCase.Output.Success -> {
                    _uiState.value = Result.Success()
                    Logger.d("AUTH", "Logout success")
                }

                is LogoutUseCase.Output.Failure -> {
                    _uiState.value = Result.Error("Error logging out ${result.message}")
                    Logger.d("AUTH", "Logout failed ${result.message}")
                }
            }
        }
    }
}
