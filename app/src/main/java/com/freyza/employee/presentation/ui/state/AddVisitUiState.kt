package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.VisitType

import kotlinx.datetime.LocalDateTime

data class AddVisitUiState(
  val today: LocalDateTime,
  val visitType: VisitType? = null,
  val creationState: UIState<Unit> = UIState.Idle()
)
