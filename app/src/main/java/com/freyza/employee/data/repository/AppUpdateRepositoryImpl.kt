package com.freyza.employee.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.network.ApiErrorResponse
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.local.dao.AppMetaDao
import com.freyza.employee.data.local.entities.AppMetaEntity
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.network.dto.AppDownloadResponseDto
import com.freyza.employee.data.network.dto.AppVersionResponseDto
import com.freyza.employee.domain.model.AppUpdateInfo
import com.freyza.employee.domain.repository.AppUpdateRepository
import com.freyza.employee.domain.repository.DownloadProgress
import io.github.jan.supabase.auth.Auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class AppUpdateRepositoryImpl(
  private val context: Context,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
  private val appMetaDao: AppMetaDao,
) : AppUpdateRepository {

  companion object {
    private const val TAG = "AppUpdateRepository"
    private const val APK_NAME = "update.apk"
  }

  override suspend fun checkForUpdate(): Result<AppUpdateInfo> {
    val now = System.currentTimeMillis()
    val meta = withContext(Dispatchers.IO) { appMetaDao.getAppMeta() } ?: AppMetaEntity()

    if ((now - meta.lastVersionCheckAt) < Constants.TTL_VERSION_CHECK_MS) {
      Logger.d(TAG, "Skipping network update check; last check was within 12h")
      return Result.Success(
        AppUpdateInfo(
          hasUpdate = false,
          versionName = BuildConfig.VERSION_NAME,
          buildNumber = BuildConfig.VERSION_CODE.toLong(),
          releaseNotes = "",
          isMandatory = false,
          downloadUrl = "",
          fileSizeMb = null
        )
      )
    }

    return safeApiCall(TAG, auth) { token ->
      withContext(Dispatchers.IO) {
        Logger.d(TAG, "Fetching latest app version from server")

        val versionResponse = httpClient.get("${appConfig.apiUrl}/api/app/version/latest") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (!versionResponse.status.isSuccess()) {
          val error = try {
            versionResponse.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = versionResponse.status.description)
          }
          Logger.e(TAG, "API Error: ${versionResponse.status} - $error")
          throw Exception(error.message)
        }

        val updateResponse = versionResponse.body<AppVersionResponseDto>()
        val versionData = updateResponse.data
          ?: throw Exception("Unable to fetch latest version")

        val localBuildNumber = BuildConfig.VERSION_CODE
        val hasUpdate = versionData.buildNumber > localBuildNumber

        appMetaDao.upsertAppMeta(
          meta.copy(
            lastVersionCheckAt = now,
            serverVersion = versionData.versionName
          )
        )

        Sentry.addBreadcrumb(Breadcrumb().apply {
          category = "app.update"
          message = "Version check completed"
          setData("localBuildNumber", localBuildNumber)
          setData("serverBuildNumber", versionData.buildNumber)
          setData("hasUpdate", hasUpdate)
          setData("isMandatory", versionData.isMandatory)
        })

        if (!hasUpdate) {
          Logger.d(TAG, "App is up to date (local: $localBuildNumber, server: ${versionData.buildNumber})")
          return@withContext versionData.toDomain(hasUpdate = false)
        }

        Logger.d(TAG, "Update available, fetching download link")
        val downloadResponse = httpClient.get("${appConfig.apiUrl}/api/app/download") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("versionName", versionData.versionName)
          parameter("buildNumber", versionData.buildNumber)
        }

        if (!downloadResponse.status.isSuccess()) {
          val error = try {
            downloadResponse.body<ApiErrorResponse>()
          } catch (e: Exception) {
            ApiErrorResponse(message = downloadResponse.status.description)
          }
          Logger.e(TAG, "Download link API Error: ${downloadResponse.status} - $error")
          throw Exception(error.message)
        }

        val downloadResult = downloadResponse.body<AppDownloadResponseDto>()
        val downloadData = downloadResult.data
          ?: throw Exception("Unable to fetch update download link")

        downloadData.toDomain(hasUpdate = true)
      }
    }
  }

  override fun downloadUpdate(downloadUrl: String): Flow<DownloadProgress> =
    flow {
      val downloadSpan = Sentry.getSpan()?.startChild("app.update.download", "Download Update APK")

      try {
        val targetFile = File(context.cacheDir, APK_NAME)
        if (targetFile.exists()) {
          targetFile.delete()
        }

        Sentry.addBreadcrumb(Breadcrumb().apply {
          category = "app.update"
          message = "Starting APK download"
          setData("targetPath", targetFile.absoluteFile)
        })

        httpClient.prepareGet(downloadUrl) {
          headers.clear()
        }.execute { response ->
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

        downloadSpan?.status = SpanStatus.OK
        Sentry.addBreadcrumb(Breadcrumb().apply {
          category = "app.update"
          message = "APK download completed"
        })
        emit(DownloadProgress.Finished)
      } catch (e: Exception) {
        downloadSpan?.status = SpanStatus.INTERNAL_ERROR
        downloadSpan?.throwable = e
        Sentry.captureException(e)
        Logger.e(TAG, "Download failed", e)
        emit(DownloadProgress.Error(e.message ?: "Unknown error occurred during download"))
      } finally {
        downloadSpan?.finish()
      }
    }.flowOn(Dispatchers.IO)

  override fun installUpdate(): Result<Unit> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!context.packageManager.canRequestPackageInstalls()) {
        Sentry.addBreadcrumb(Breadcrumb().apply {
          category = "app.update"
          message = "Redirecting user to unknown app sources permission setting"
        })

        val intent =
          Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .apply {
              data = "package:${context.packageName}".toUri()
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
        return Result.Error("Permission required to install unknown apps")
      }
    }

    val apkFile = File(context.cacheDir, APK_NAME)
    if (!apkFile.exists()) {
      val errorMsg = "APK file not found at ${apkFile.absolutePath}"
      Sentry.captureMessage(errorMsg)
      return Result.Error("APK file not found")
    }

    return try {
      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      Sentry.addBreadcrumb(Breadcrumb().apply {
        category = "app.update"
        message = "Launching Android Package Installer intent"
        setData("uri", contentUri.toString())
      })

      context.startActivity(intent)
      Result.Success(Unit)
    } catch (e: Exception) {
      Logger.e(TAG, "Installation failed", e)

      Sentry.captureException(e)
      Result.Error("Failed to start installer: ${e.message}")
    }
  }

  private fun HttpResponse.contentLength(): Long? {
    return headers["Content-Length"]?.toLongOrNull()
  }
}
