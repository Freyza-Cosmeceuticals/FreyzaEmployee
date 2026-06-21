package com.freyza.employee.core.state

import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.User
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
        Logger.i(TAG, "Employee set to $employee")
    }

    fun clearSession() {
        _currentEmployee.value = null
        Logger.i(TAG, "Session was cleared")
    }
}
