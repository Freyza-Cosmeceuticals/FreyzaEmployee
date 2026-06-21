package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.AuthState
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationRepositoryImpl(
  private val auth: Auth,
  private val userRepository: UserRepository,
  private val sessionManager: SessionManager,
) : AuthenticationRepository {

  companion object {
    const val TAG = "AuthRepository"
  }

  private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()

  init {
    listenToAuthStatus()
  }

  private fun listenToAuthStatus() {
    repositoryScope.launch {
      auth.sessionStatus.collect { status ->
        when (status) {
          is SessionStatus.Authenticated -> {
            Logger.d(TAG, "Authenticated: fetching profile")
            validateAndFetchProfile(status.session.user?.id ?: "")
          }

          is SessionStatus.NotAuthenticated -> {
            Logger.d(TAG, "NotAuthenticated")
            sessionManager.clearSession()
            _authState.value = AuthState.Unauthenticated
          }

          SessionStatus.Initializing -> {
            Logger.d(TAG, "Initializing")
            _authState.value = AuthState.Loading
          }

          is SessionStatus.RefreshFailure -> {
            Logger.e(TAG, "RefreshFailure")
            _authState.value = AuthState.Error("Session refresh failed. Please check your connection.")
          }
        }
      }
    }
  }

  override suspend fun checkSession() {
    try {
      _authState.value = AuthState.Loading
      auth.awaitInitialization()
      val session = auth.currentSessionOrNull()
      val user = auth.currentUserOrNull()

      if (session != null && user != null) {
        validateAndFetchProfile(user.id)
      } else {
        sessionManager.clearSession()
        _authState.value = AuthState.Unauthenticated
      }
    } catch (e: Exception) {
      Logger.e(TAG, "checkSession error: ${e.message}")
      sessionManager.clearSession()
      _authState.value = AuthState.Error("Failed to initialize session: ${e.message}")
    }
  }

  private suspend fun validateAndFetchProfile(userId: String) {
    if (userId.isBlank()) {
      _authState.value = AuthState.Unauthenticated
      return
    }

    when (val result = userRepository.getUserById(userId)) {
      is Result.Success -> {
        val user = result.data
        if (user != null && user.role == UserRole.EMPLOYEE && user.status == UserStatus.ACTIVE) {
          val supabaseUser = auth.currentUserOrNull()
          val finalUser = user.copy(userInfo = supabaseUser)
          sessionManager.setCurrentEmployee(finalUser)
          _authState.value = AuthState.Authenticated
        } else {
          Logger.e(TAG, "Invalid role or inactive status. Role: ${user?.role}, Status: ${user?.status}")
          logout()
          _authState.value = AuthState.Unauthenticated
        }
      }

      is Result.Error -> {
        Logger.e(TAG, "Error fetching user profile: ${result.message}")
        _authState.value = AuthState.Error("Unable to fetch your profile: ${result.message}")
      }

      is Result.Loading -> {
        _authState.value = AuthState.Loading
      }
    }
  }

  override suspend fun login(email: String, password: String): Result<UserInfo> {
    return try {
      auth.signInWith(Email) {
        this.email = email
        this.password = password
      }

      val user = auth.currentUserOrNull()
      if (user === null) {
        Logger.e(TAG, "Login Error: $email, Current user is null")
        return Result.Error("Unable to login")
      }

      // Role check happens via the sessionStatus listener calling validateAndFetchProfile
      Result.Success(user)

    } catch (e: Exception) {
      val cause = e.message?.lines()?.first().toString().trim()
      Logger.e(TAG, "Login Error: $email ${e.message.toString()}")
      Result.Error(cause)
    }
  }

  override suspend fun register(name: String, email: String, password: String): Result<UserInfo> {
    return Result.Error("Registration not implemented in app")
  }

  override suspend fun loginWithGoogle(): Result<UserInfo> {
    return try {
      auth.signInWith(Google)
      val user = auth.currentUserOrNull()
      if (user === null) {
        Logger.e(TAG, "Login Error: Google, Current user is null")
        return Result.Error("Unable to login")
      }

      Result.Success(user)
    } catch (e: Exception) {
      Result.Error(e.message.toString())
    }
  }

  override suspend fun logout(): Result<Unit> {
    return try {
      auth.signOut(SignOutScope.LOCAL)
      sessionManager.clearSession()
      Logger.i(TAG, "Logout Success")
      Result.Success(Unit)
    } catch (e: Exception) {
      val cause = e.message?.lines()?.first().toString().trim()
      Logger.e(TAG, "Logout Error: ${e.message.toString()}")
      Result.Error(cause)
    }
  }

}
