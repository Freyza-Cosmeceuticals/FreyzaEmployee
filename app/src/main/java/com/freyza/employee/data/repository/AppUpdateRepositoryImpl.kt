package com.freyza.employee.data.repository

import androidx.core.net.toUri
import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.AppVersionResponseDto
import com.freyza.employee.domain.model.AppUpdateInfo
import com.freyza.employee.domain.repository.AppUpdateRepository
import com.freyza.employee.domain.repository.DownloadProgress
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class AppUpdateRepositoryImpl(
  private val context: android.content.Context,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
) : AppUpdateRepository {

  companion object {
    private const val TAG = "AppUpdateRepository"
    private const val APK_NAME = "update.apk"
  }

  override suspend fun checkForUpdate(): Result<AppUpdateInfo> {
    return safeApiCall(TAG) {
      withContext(Dispatchers.IO) {
        val token = auth.currentAccessTokenOrNull() ?: run {
          Logger.d(TAG, "Token not found immediately, waiting for session status...")
          auth.sessionStatus
            .filterIsInstance<SessionStatus.Authenticated>()
            .map { it.session.accessToken }
            .firstOrNull()
        } ?: throw IllegalStateException("No authentication token found")

        val response = httpClient.get("${appConfig.apiUrl}/api/app/version/latest") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status.isSuccess()) {
          val updateResponse = response.body<AppVersionResponseDto>()
          if (updateResponse.success) {
            val localBuildNumber = BuildConfig.VERSION_CODE
            val hasUpdate = updateResponse.data.buildNumber > localBuildNumber

            updateResponse.data.toDomain(hasUpdate = hasUpdate)
          } else {
            throw Exception("Unable to fetch latest version")
          }
        } else {
          val error = try {
            response.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = response.status.description)
          }
          Logger.e(TAG, "API Error: ${response.status} - $error")
          throw Exception(error.message)
        }
      }
    }
  }

  override fun downloadUpdate(downloadUrl: String): Flow<DownloadProgress> =
    flow {
      try {
        val targetFile = File(context.cacheDir, APK_NAME)
        if (targetFile.exists()) {
          targetFile.delete()
        }

        httpClient.prepareGet(downloadUrl).execute { response ->
          val channel = response.bodyAsChannel()
          val contentLength = response.contentLength() ?: -1L
          var totalBytesRead = 0L
          val buffer = ByteArray(8 * 1024)
          var lastEmittedPercent = -1

          targetFile.outputStream().use { output ->
            while (!channel.isClosedForRead) {
              val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
              if (bytesRead == -1) break

              output.write(buffer, 0, bytesRead)
              totalBytesRead += bytesRead

              if (contentLength > 0) {
                val currentPercent = (totalBytesRead * 100 / contentLength).toInt()
                if (currentPercent > lastEmittedPercent) {
                  lastEmittedPercent = currentPercent
                  emit(DownloadProgress.Progress(currentPercent.toFloat()))
                }
              }
            }
          }
        }
        emit(DownloadProgress.Finished)
      } catch (e: Exception) {
        Logger.e(TAG, "Download failed", e)
        emit(DownloadProgress.Error(e.message ?: "Unknown error occurred during download"))
      }
    }.flowOn(Dispatchers.IO)

  override fun installUpdate(): Result<Unit> {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      if (!context.packageManager.canRequestPackageInstalls()) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
          data = "package:${context.packageName}".toUri()
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.Error("Permission required to install unknown apps")
      }
    }

    val apkFile = File(context.cacheDir, APK_NAME)
    if (!apkFile.exists()) {
      return Result.Error("APK file not found")
    }

    return try {
      val contentUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )

      val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      Result.Success(Unit)
    } catch (e: Exception) {
      Logger.e(TAG, "Installation failed", e)
      Result.Error("Failed to start installer: ${e.message}")
    }
  }

  private fun io.ktor.client.statement.HttpResponse.contentLength(): Long? {
    return headers["Content-Length"]?.toLongOrNull()
  }
}
