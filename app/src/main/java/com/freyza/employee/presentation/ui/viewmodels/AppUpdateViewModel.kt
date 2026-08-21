package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.repository.AppUpdateRepository
import com.freyza.employee.domain.repository.DownloadProgress
import com.freyza.employee.presentation.ui.state.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppUpdateViewModel(
  private val repository: AppUpdateRepository,
) : ViewModel() {

  companion object {
    private const val TAG = "AppUpdateViewModel"
  }

  private val _uiState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
  val uiState: StateFlow<AppUpdateState> = _uiState.asStateFlow()

  fun checkForUpdate() {
    if (_uiState.value is AppUpdateState.Checking ||
      _uiState.value is AppUpdateState.Downloading ||
      _uiState.value is AppUpdateState.ReadyToInstall
    ) {
      Logger.d(TAG, "Skipping update check: state is ${_uiState.value}")
      return
    }

    Logger.d(TAG, "Checking for app update")
    _uiState.value = AppUpdateState.Checking

    viewModelScope.launch {
      when (val result = repository.checkForUpdate()) {
        is Result.Success -> {
          if (result.data.hasUpdate) {
            Logger.d(TAG, "Update available: ${result.data}")
            _uiState.value = AppUpdateState.UpdateAvailable(result.data)
          } else {
            Logger.d(TAG, "Already up to date")
            _uiState.value = AppUpdateState.Idle
          }
        }

        is Result.Error -> {
          Logger.e(TAG, "Update check failed: ${result.message}")
          _uiState.value = AppUpdateState.Idle
        }

        else -> {}
      }
    }
  }

  fun startDownload() {
    Logger.d(TAG, "Starting update download")

    val currentState = _uiState.value
    if (currentState !is AppUpdateState.UpdateAvailable) return

    val updateInfo = currentState.updateInfo

    viewModelScope.launch {
      repository.downloadUpdate(updateInfo.downloadUrl).collect { progress ->
        when (progress) {
          is DownloadProgress.Progress -> {
            Logger.d(TAG, "Download progress: ${progress.percentage}")
            _uiState.value = AppUpdateState.Downloading(updateInfo, progress.percentage)
          }

          is DownloadProgress.Finished -> {
            Logger.d(TAG, "Download finished")
            _uiState.value = AppUpdateState.ReadyToInstall(updateInfo)
          }

          is DownloadProgress.Error -> {
            Logger.e(TAG, "Download error: ${progress.message}")
            _uiState.value = AppUpdateState.Error(progress.message)
          }
        }
      }
    }
  }

  fun installUpdate() {
    val currentState = _uiState.value
    if (currentState !is AppUpdateState.ReadyToInstall) return

    Logger.d(TAG, "Installing update")

    when (val result = repository.installUpdate()) {
      is Result.Error -> {
        _uiState.value = AppUpdateState.Error(result.message)
      }
      else -> {
        // Installation intent launched successfully or redirected to settings
      }
    }
  }

  fun dismissUpdate() {
    Logger.d(TAG, "Dismissing update")
    _uiState.value = AppUpdateState.Idle
  }
}
