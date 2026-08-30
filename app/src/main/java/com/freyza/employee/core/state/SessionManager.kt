package com.freyza.employee.core.state

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.SharedPreferencesHelper
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toDto
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Holds current employee session.
 */
class SessionManager(
  private val sharedPreferencesHelper: SharedPreferencesHelper,
) {

  companion object {
    const val TAG = "SessionManager"
    private const val KEY_CACHED_USER = "cached_user_profile"
    private val json = Json {
      ignoreUnknownKeys = true
      encodeDefaults = true
    }
  }

  private val _currentEmployee: MutableStateFlow<User?>

  init {
    val cachedUser = loadUserFromDisk()
    _currentEmployee = MutableStateFlow(cachedUser)
    if (cachedUser != null) {
      Logger.i(TAG, "Hydrated cached employee: ${cachedUser.id}")
      updateSentryUser(cachedUser)
    }
  }

  val currentEmployee = _currentEmployee.asStateFlow()

  fun setCurrentEmployee(employee: User?) {
    _currentEmployee.value = employee
    Logger.i(TAG, "Employee set to ${employee?.id}")

    saveUserToDisk(employee)
    updateSentryUser(employee)
  }

  fun clearSession() {
    _currentEmployee.value = null
    Logger.i(TAG, "Session was cleared")

    sharedPreferencesHelper.removeStringData(KEY_CACHED_USER)

    if (!BuildConfig.DEBUG) {
      sentryClearUser()
    }
  }

  private fun loadUserFromDisk(): User? {
    return try {
      val rawJson = sharedPreferencesHelper.getStringData(KEY_CACHED_USER)
      if (!rawJson.isNullOrBlank()) {
        json.decodeFromString<UserDto>(rawJson).toDomain()
      } else {
        null
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to load cached user from disk: ${e.message}")
      null
    }
  }

  private fun saveUserToDisk(employee: User?) {
    try {
      if (employee != null) {
        val rawJson = json.encodeToString(UserDto.serializer(), employee.toDto())
        sharedPreferencesHelper.saveStringData(KEY_CACHED_USER, rawJson)
      } else {
        sharedPreferencesHelper.removeStringData(KEY_CACHED_USER)
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to save user to disk: ${e.message}")
    }
  }

  private fun updateSentryUser(employee: User?) {
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

  private fun sentryClearUser() {
    Sentry.setUser(null)
    Sentry.removeTag("user.role")
    Sentry.removeTag("user.hq_id")
  }
}
