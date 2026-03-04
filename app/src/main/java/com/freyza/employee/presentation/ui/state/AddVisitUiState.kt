package com.freyza.employee.presentation.ui.state

import com.freyza.employee.domain.model.VisitType

data class AddVisitUiState(
  val visitType: VisitType? = null,
)
