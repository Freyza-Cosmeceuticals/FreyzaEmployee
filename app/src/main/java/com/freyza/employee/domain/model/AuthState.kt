package com.freyza.employee.domain.model

sealed class AuthState {
  object Loading : AuthState()
  object Authenticated : AuthState()
  object Unauthenticated : AuthState()
  data class Error(val message: String) : AuthState()
}
