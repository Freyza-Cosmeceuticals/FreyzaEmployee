package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.User

data class ProfileScreenUiState(
  val user: UIState<User> = UIState.Idle()
)
