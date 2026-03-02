package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.Constants
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyUserEmployee
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class MainUiState(
  val hasValidSession: Boolean = false,
  val user: User? = null,
  val today: LocalDateTime? = null,
)

fun dummyMainUiState(): MainUiState = MainUiState(
  hasValidSession = true, user = dummyUserEmployee(), today = Clock.System.now().toLocalDateTime(
    TimeZone.of(Constants.TIMEZONE)
  )
)
