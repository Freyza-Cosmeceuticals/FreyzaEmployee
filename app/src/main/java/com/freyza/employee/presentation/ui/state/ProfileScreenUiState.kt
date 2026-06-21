package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.util.ServerTime
import kotlinx.datetime.LocalDateTime

data class ProfileScreenUiState(
  val today: LocalDateTime,
)

fun dummyProfileScreenUiState(serverTime: ServerTime = ServerTime()): ProfileScreenUiState =
  ProfileScreenUiState(
    today = serverTime.nowLocalDateTime()
  )
