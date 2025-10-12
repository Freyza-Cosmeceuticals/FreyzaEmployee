package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.freyza.employee.common.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow<Result<Nothing>?>(null)
    val uiState = _uiState.asStateFlow()
}
