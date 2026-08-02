package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.NetworkMonitor
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AuthenticationRepositoryImpl(
  private val auth: Auth,
  private val userRepository: UserRepository,
  private val sessionManager: SessionManager,
  private val networkMonitor: NetworkMonitor,
) : AuthenticationRepository {

  companion object {
    const val TAG = "AuthRepository"
  }

  private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()

  init {
    listenToAuthStatus()
    logAuthState()
    observeNetworkForAutoRefresh()
  }

  private fun observeNetworkForAutoRefresh() {
    networkMonitor.isOnline
      .drop(1)
      .filter { it }
      .onEach {
        Logger.d(TAG, "Network back online, triggering auto-refresh")
        checkSession()
      }
      .launchIn(repositoryScope)
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
            sessionManager.clearSession()
            _authState.value =
              AuthState.Error("Session refresh failed. Please check your connection.")
          }
        }
      }
    }
  }

  private fun logAuthState() {
    authState.onEach { state ->
      when (state) {
        is AuthState.Authenticated -> {
          Logger.i(TAG, "AuthState: Authenticated")
        }

        is AuthState.Error -> {
          Logger.e(TAG, "AuthState: Error. ${state.message}")
        }

        is AuthState.Loading -> {
          Logger.i(TAG, "AuthState: Loading")
        }

        is AuthState.Unauthenticated -> {
          Logger.i(TAG, "AuthState: Unauthenticated")
        }
      }
    }.launchIn(repositoryScope)
  }

  override suspend fun checkSession() {
    try {
      auth.awaitInitialization()
      if (auth.currentUserOrNull() != null) {
        if (networkMonitor.isCurrentlyConnected) {
          auth.refreshCurrentSession()
        } else {
          Logger.d(TAG, "checkSession: Device is offline, skipping refresh")
        }
      } else {
        Logger.d(TAG, "checkSession: No current user found, setting to Unauthenticated")
        _authState.value = AuthState.Unauthenticated
      }
    } catch (e: Exception) {
      if (!networkMonitor.isCurrentlyConnected || isNetworkException(e.message ?: "")) {
        Logger.w(TAG, "checkSession: Connectivity issue occurred, showing friendly error")
        _authState.value = AuthState.Error("Please check your internet connection.")
        return
      }

      if (e.message?.contains("No refresh token", ignoreCase = true) == true) {
        Logger.w(TAG, "checkSession: No refresh token found, setting to Unauthenticated")
        _authState.value = AuthState.Unauthenticated
      } else {
        Logger.e(TAG, "checkSession error: ${e.message}")
        sessionManager.clearSession()
        _authState.value = AuthState.Error("Failed to initialize session: ${e.message}")
      }
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
          Logger.e(
            TAG,
            "Invalid role or inactive status. Role: ${user?.role}, Status: ${user?.status}"
          )
          logout()
          _authState.value = AuthState.Unauthenticated
        }
      }

      is Result.Error -> {
        if (!networkMonitor.isCurrentlyConnected || isNetworkException(result.message)
        ) {
          _authState.value = AuthState.Error("Please check your internet connection.")
        } else {
          Logger.e(TAG, "Error fetching user profile: ${result.message}")
          _authState.value = AuthState.Error("Unable to fetch your profile: ${result.message}")
        }
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

  private fun isNetworkException(message: String): Boolean {
    return message.contains("unable to resolve host", ignoreCase = true) ||
            message.contains("failed to connect", ignoreCase = true) ||
            message.contains("connecttimeout", ignoreCase = true) ||
            message.contains("unknownhost", ignoreCase = true)
  }
}
