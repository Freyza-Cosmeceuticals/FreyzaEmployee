package com.freyza.employee.data.network.dto

import kotlinx.serialization.Serializable

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
  val downloadUrl: String,
  val fileSizeMb: Double? = null,
  val expiresIn: Int? = null
)
