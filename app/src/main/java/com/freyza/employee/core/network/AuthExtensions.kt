package com.freyza.employee.core.network

import com.freyza.employee.core.util.Logger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Safely retrieves the current Supabase access token, awaiting initialization
 * or active session if the token is not yet loaded into memory.
 */
suspend fun Auth.getAccessToken(): String? {
  currentAccessTokenOrNull()?.let { return it }

  try {
    awaitInitialization()
  } catch (e: Exception) {
    Logger.w("AuthExtensions", "awaitInitialization error: ${e.message}")
  }

  currentAccessTokenOrNull()?.let { return it }

  return try {
    withTimeoutOrNull(3.seconds) {
      sessionStatus
        .filterIsInstance<SessionStatus.Authenticated>()
        .map { it.session.accessToken }
        .firstOrNull()
    }
  } catch (_: Exception) {
    null
  }
}
