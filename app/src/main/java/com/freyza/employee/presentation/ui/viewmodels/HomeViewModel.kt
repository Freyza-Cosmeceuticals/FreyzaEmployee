package com.freyza.employee.presentation.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.common.Result
import com.freyza.employee.domain.usecase.LogoutUseCase
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
                    Log.d("AUTH", "Logout success")
                }

                else -> {
                    _uiState.value = Result.Error("Error logging out")
                    Log.d("AUTH", "Logout failed")
                }
            }
        }
    }
}
