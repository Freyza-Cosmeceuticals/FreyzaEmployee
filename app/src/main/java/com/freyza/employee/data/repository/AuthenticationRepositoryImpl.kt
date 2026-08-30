package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.network.NetworkMonitor
import com.freyza.employee.core.network.isConnectivityOrDnsException
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.AuthState
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.sentry.Breadcrumb
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
  private var fetchProfileJob: Job? = null

  private val _authState = MutableStateFlow<AuthState>(
    if (sessionManager.currentEmployee.value != null) AuthState.Authenticated else AuthState.Loading
  )
  override val authState: StateFlow<AuthState> = _authState.asStateFlow()

  init {
    listenToSessionStatus()
    listenToAuthEvents()
    logAuthState()
    observeNetworkForAutoRefresh()
  }

  @OptIn(SupabaseExperimental::class)
  private fun listenToAuthEvents() {
    auth.events.onEach { event ->
      when (event) {
        is AuthEvent.RefreshFailure -> {
          Logger.w(TAG, "Refresh Failure: ${event.cause}. Checking local session...")
          Sentry.addBreadcrumb(Breadcrumb().apply {
            category = "auth"
            message = "AuthEvent: RefreshFailure (${event.cause})"
            level = io.sentry.SentryLevel.WARNING
          })
          resolveAuthenticatedState()
        }

        else -> Logger.d(TAG, "AuthEvent: ${event.javaClass.simpleName}")
      }
    }.launchIn(repositoryScope)
  }

  private fun observeNetworkForAutoRefresh() {
    var wasOffline = !networkMonitor.isCurrentlyConnected
    networkMonitor.isOnline.onEach { isOnline ->
      if (wasOffline && isOnline) {
        Logger.d(TAG, "Connectivity restored, syncing session...")
        checkSession()
      }
      wasOffline = !isOnline
    }.launchIn(repositoryScope)
  }

  private fun listenToSessionStatus() {
    repositoryScope.launch {
      auth.sessionStatus.collect { status ->
        Sentry.addBreadcrumb(Breadcrumb().apply {
          category = "auth"
          message = "SessionStatus: ${status.javaClass.simpleName}"
        })

        when (status) {
          is SessionStatus.Authenticated -> {
            Logger.d(TAG, "SessionStatus: Authenticated. Syncing profile...")
            resolveAuthenticatedState(status.session.user?.id)
          }

          is SessionStatus.NotAuthenticated -> {
            val cachedUser = sessionManager.currentEmployee.value
            if (cachedUser != null) {
              Logger.w(
                TAG,
                "Session not found but offline with cache. Preserving Authenticated state."
              )
              _authState.value = AuthState.Authenticated
            } else {
              Logger.i(TAG, "Session cleared: Not authenticated (online or no cache)")
              sessionManager.clearSession()
              _authState.value = AuthState.Unauthenticated
            }
          }

          SessionStatus.Initializing -> {
            Logger.d(TAG, "SessionStatus: Initializing")
            _authState.value = AuthState.Loading
          }

          is SessionStatus.RefreshFailure -> {
            Logger.w(TAG, "SessionStatus: Refresh failure. Resolving resilient state...")
            resolveAuthenticatedState()
          }
        }
      }
    }
  }

  private fun resolveAuthenticatedState(userId: String? = null) {
    val cachedUser = sessionManager.currentEmployee.value
    val targetId = userId ?: auth.currentUserOrNull()?.id ?: auth.currentSessionOrNull()?.user?.id

    if (cachedUser != null) {
      _authState.value = AuthState.Authenticated
    } else if (!targetId.isNullOrBlank()) {
      repositoryScope.launch {
        validateAndFetchProfile(targetId)
      }
    } else {
      _authState.value = AuthState.Unauthenticated
    }
  }

  private fun logAuthState() {
    authState.onEach { state ->
      Sentry.addBreadcrumb(Breadcrumb().apply {
        category = "auth"
        message = "AuthState: ${state.javaClass.simpleName}"
      })

      when (state) {
        is AuthState.Authenticated -> Logger.i(TAG, "AuthState: Authenticated")
        is AuthState.Error -> Logger.e(TAG, "AuthState: Error. ${state.message}")
        is AuthState.Loading -> Logger.d(TAG, "AuthState: Loading")
        is AuthState.Unauthenticated -> Logger.i(TAG, "AuthState: Unauthenticated")
      }
    }.launchIn(repositoryScope)
  }

  override suspend fun checkSession() {
    try {
      auth.awaitInitialization()
      if (networkMonitor.isCurrentlyConnected && auth.currentSessionOrNull() != null) {
        try {
          auth.refreshCurrentSession()
        } catch (e: Exception) {
          if (e.isConnectivityOrDnsException() || !networkMonitor.isCurrentlyConnected) {
            Logger.d(TAG, "Silent refresh failed (network). Preserving current state.")
          } else if (e.message?.contains("invalid_grant", ignoreCase = true) == true) {
            Logger.w(TAG, "Refresh token revoked. Logging out.")
            sessionManager.clearSession()
            _authState.value = AuthState.Unauthenticated
            return
          }
        }
      }
      resolveAuthenticatedState()
    } catch (e: Exception) {
      val isNetworkErr = e.isConnectivityOrDnsException() || !networkMonitor.isCurrentlyConnected
      if (isNetworkErr) {
        if (sessionManager.currentEmployee.value != null) {
          Logger.w(TAG, "Offline/Flaky network. Preserving current session.")
          _authState.value = AuthState.Authenticated
        } else {
          _authState.value = AuthState.Error("Please check your internet connection.")
        }
      } else if (e.message?.contains("No refresh token", ignoreCase = true) == true) {
        _authState.value = AuthState.Unauthenticated
      } else {
        Logger.e(TAG, "Critical session check failure: ${e.message}")
        if (sessionManager.currentEmployee.value != null) {
          _authState.value = AuthState.Authenticated
        } else {
          sessionManager.clearSession()
          _authState.value = AuthState.Error("Failed to initialize session.")
        }
      }
    }
  }

  private suspend fun validateAndFetchProfile(userId: String) {
    if (userId.isBlank()) {
      _authState.value = AuthState.Unauthenticated
      return
    }

    if (sessionManager.currentEmployee.value?.id == userId) {
      _authState.value = AuthState.Authenticated
      return
    }

    if (fetchProfileJob?.isActive == true) {
      fetchProfileJob?.join()
      return
    }

    fetchProfileJob = repositoryScope.launch {
      when (val result = userRepository.getUserById(userId)) {
        is Result.Success -> {
          val user = result.data
          if (user != null && user.role == UserRole.EMPLOYEE && user.status == UserStatus.ACTIVE) {
            sessionManager.setCurrentEmployee(user.copy(userInfo = auth.currentUserOrNull()))
            _authState.value = AuthState.Authenticated
          } else {
            Logger.e(
              TAG,
              "Account issue: User is not an active employee (Role: ${user?.role}, Status: ${user?.status})"
            )
            logout()
            _authState.value = AuthState.Unauthenticated
          }
        }

        is Result.Error -> {
          val isNetworkIssue =
            (result.errorBody as? Throwable)?.isConnectivityOrDnsException() == true ||
                    !networkMonitor.isCurrentlyConnected

          if (isNetworkIssue && sessionManager.currentEmployee.value != null) {
            Logger.d(TAG, "Profile fetch failed (network). Using cached profile.")
            _authState.value = AuthState.Authenticated
          } else if (isNetworkIssue) {
            _authState.value = AuthState.Error("Please check your internet connection.")
          } else {
            Logger.e(TAG, "Profile fetch failed: ${result.message}")
            _authState.value = AuthState.Error("Unable to fetch your profile.")
          }
        }

        is Result.Loading -> _authState.value = AuthState.Loading
      }
    }
    fetchProfileJob?.join()
  }

  override suspend fun login(email: String, password: String): Result<UserInfo> = try {
    auth.signInWith(Email) {
      this.email = email
      this.password = password
    }
    auth.currentUserOrNull()?.let {
      Logger.i(TAG, "Login successful: $email")
      Result.Success(it)
    } ?: Result.Error("Login returned empty user")
  } catch (e: Exception) {
    val cause = e.message?.lines()?.firstOrNull()?.trim() ?: "Login failed"
    Logger.w(TAG, "Login failed: $email ($cause)")
    Result.Error(cause)
  }

  override suspend fun register(name: String, email: String, password: String): Result<UserInfo> =
    Result.Error("Registration not implemented in app")

  override suspend fun loginWithGoogle(): Result<UserInfo> = try {
    auth.signInWith(Google)
    auth.currentUserOrNull()?.let {
      Logger.i(TAG, "Google login successful")
      Result.Success(it)
    } ?: Result.Error("Google login returned empty user")
  } catch (e: Exception) {
    Logger.w(TAG, "Google login failed: ${e.message}")
    Result.Error(e.message ?: "Google login failed")
  }

  override suspend fun logout(): Result<Unit> = try {
    auth.signOut(SignOutScope.LOCAL)
    Logger.i(TAG, "Logout successful")
    Result.Success(Unit)
  } catch (e: Exception) {
    val cause = e.message?.lines()?.firstOrNull()?.trim() ?: "Logout failed"
    Logger.w(TAG, "Logout warning: $cause")
    Result.Success(Unit) // Still return success as we clear local state anyway
  } finally {
    sessionManager.clearSession()
    _authState.value = AuthState.Unauthenticated
  }
}
