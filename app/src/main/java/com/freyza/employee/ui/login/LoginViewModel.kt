package com.freyza.employee.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.data.models.User
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Result<User>?>(null)
    val loginResult: StateFlow<Result<User>?> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        _uiState.value = Result.Progress
        viewModelScope.launch {
            val result = userRepository.login(username, password)
            _uiState.value = result
        }
    }

    fun logout() {
        _uiState.value = Result.Progress
        viewModelScope.launch {
            userRepository.logout()
            _uiState.value = null
        }
    }
}
