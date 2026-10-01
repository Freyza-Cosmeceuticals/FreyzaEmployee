package com.freyza.employee.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Response DTO for the latest version check endpoint (/api/app/version/latest).
 */
@Serializable
data class AppVersionResponseDto(
  val success: Boolean,
  val data: AppVersionDataDto?
)

@Serializable
data class AppVersionDataDto(
  val versionName: String,
  val buildNumber: Long,
  val releaseNotes: String? = null,
  val isMandatory: Boolean = false,
  val fileSizeMb: Double? = null
)

/**
 * Response DTO for the download URL endpoint (/api/app/download?versionName={}&buildNumber={}).
 */
@Serializable
data class AppDownloadResponseDto(
  val success: Boolean,
  val data: AppDownloadDataDto?
)

@Serializable
data class AppDownloadDataDto(
  val versionName: String,
  val buildNumber: Long,
  val releaseNotes: String? = null,
  val isMandatory: Boolean = false,
  val downloadUrl: String,
  val expiresIn: Int? = null,
  val fileSizeMb: Double? = null
)
