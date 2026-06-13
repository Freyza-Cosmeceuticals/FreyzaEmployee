package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyUserEmployee
import kotlinx.datetime.LocalDateTime

data class MainUiState(
  val hasValidSession: Boolean = false,
  val user: User? = null,
  val today: LocalDateTime,
)

fun dummyMainUiState(serverTime: ServerTime = ServerTime()): MainUiState = MainUiState(
  hasValidSession = true,
  user = dummyUserEmployee(),
  today = serverTime.nowLocalDateTime()
)
