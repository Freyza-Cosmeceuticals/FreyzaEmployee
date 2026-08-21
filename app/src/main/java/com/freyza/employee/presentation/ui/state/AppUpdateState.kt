package com.freyza.employee.presentation.ui.state

import com.freyza.employee.domain.model.AppUpdateInfo

sealed class AppUpdateState {
  data object Idle : AppUpdateState()
  data object Checking : AppUpdateState()
  data class UpdateAvailable(val updateInfo: AppUpdateInfo) : AppUpdateState()
  data class Downloading(val updateInfo: AppUpdateInfo, val progress: Float) : AppUpdateState()
  data class ReadyToInstall(val updateInfo: AppUpdateInfo) : AppUpdateState()
  data class Error(val message: String) : AppUpdateState()
}
