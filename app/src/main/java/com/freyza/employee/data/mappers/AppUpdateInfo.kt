package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.AppVersionDataDto
import com.freyza.employee.domain.model.AppUpdateInfo

fun AppVersionDataDto.toDomain(hasUpdate: Boolean) = AppUpdateInfo(
  hasUpdate = hasUpdate,
  versionName = versionName,
  buildNumber = buildNumber,
  releaseNotes = releaseNotes ?: "",
  isMandatory = isMandatory,
  downloadUrl = downloadUrl,
  fileSizeMb = fileSizeMb
)
