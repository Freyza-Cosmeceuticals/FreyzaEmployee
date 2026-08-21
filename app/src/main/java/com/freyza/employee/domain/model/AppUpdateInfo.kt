package com.freyza.employee.domain.model

data class AppUpdateInfo(
  val hasUpdate: Boolean,
  val versionName: String,
  val buildNumber: Long,
  val releaseNotes: String,
  val isMandatory: Boolean,
  val downloadUrl: String,
  val fileSizeMb: Double?,
)

fun dummyAppUpdateInfoAvailable(mandatory: Boolean = false) = AppUpdateInfo(
  hasUpdate = true,
  versionName = "1.0.2",
  buildNumber = 23,
  releaseNotes = "This new update features a lot of things",
  isMandatory = mandatory,
  downloadUrl = "",
  fileSizeMb = 29.9
)
