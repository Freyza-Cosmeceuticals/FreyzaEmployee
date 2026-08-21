package com.freyza.employee.domain.repository

import com.freyza.employee.core.Result
import com.freyza.employee.domain.model.AppUpdateInfo
import java.io.File
import kotlinx.coroutines.flow.Flow

interface AppUpdateRepository {
  suspend fun checkForUpdate(): Result<AppUpdateInfo>
  fun downloadUpdate(downloadUrl: String): Flow<DownloadProgress>
  fun installUpdate(): Result<Unit>
}

sealed class DownloadProgress {
  data class Progress(val percentage: Float) : DownloadProgress()
  data object Finished : DownloadProgress()
  data class Error(val message: String) : DownloadProgress()
}
