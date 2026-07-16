package com.freyza.employee.core.state

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.User
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds current employee session.
 */
class SessionManager {

  companion object {
    const val TAG = "SessionManager"
  }

  private val _currentEmployee = MutableStateFlow<User?>(null)
  val currentEmployee = _currentEmployee.asStateFlow()

  fun setCurrentEmployee(employee: User?) {
    _currentEmployee.value = employee
    Logger.i(TAG, "Employee set to ${employee?.id}")

    if (!BuildConfig.DEBUG) {
      if (employee != null) {
        val sentryUser = io.sentry.protocol.User().apply {
          id = employee.id
          email = employee.email
          username = employee.name
        }
        Sentry.setUser(sentryUser)

        Sentry.setTag("user.role", employee.role.name)
        employee.hqId?.let { Sentry.setTag("user.hq_id", it) }
      } else {
        sentryClearUser()
      }
    }
  }

  fun clearSession() {
    _currentEmployee.value = null
    Logger.d(TAG, "Session was cleared")

    if (!BuildConfig.DEBUG) {
      sentryClearUser()
    }
  }

  private fun sentryClearUser() {
    Sentry.setUser(null)
    Sentry.removeTag("user.role")
    Sentry.removeTag("user.hq_id")
  }
}
