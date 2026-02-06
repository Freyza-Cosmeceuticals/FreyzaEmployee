package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import com.freyza.employee.presentation.ui.state.MainUiState
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


class MainViewModel(
  private val auth: Auth,
  val getUserUseCase: GetUserUseCase,
  val logoutUseCase: LogoutUseCase,
  private val sessionManager: SessionManager,
) : ViewModel() {

  companion object {
    const val TAG = "MainViewModel"
  }

  private val _uiState = MutableStateFlow<UIState<MainUiState>>(UIState.Idle())
  val uiState =
    _uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UIState.Idle())

  // SharedFlow to emit debug/toast messages.
  private val _toastMessageFlow = MutableSharedFlow<String>()
  val toastMessageFlow = _toastMessageFlow.asSharedFlow()


  init {
    Logger.i(TAG, "Init")
    // does not actually clear the saved session, but the sessionManagerRepo state
    sessionManager.clearSession()

    initializeSession()
    listenToAuthEvents()    // also updates UI state for now
    listenToUiState()
  }

  fun initializeSession() {
    Logger.i(TAG, "Initializing Session")
    viewModelScope.launch(Dispatchers.IO) {
      _uiState.update {
        UIState.Loading(
          it.data?.copy(today = getTodayDate()) ?: MainUiState(today = getTodayDate())
        )
      }

      try {
        auth.awaitInitialization()

        val session = auth.currentSessionOrNull()
        val supabaseUser = auth.currentUserOrNull()
        val validSession = session != null && supabaseUser != null

        Logger.d(TAG, "From storage, Session: ${session}, User: $supabaseUser")

        withContext(Dispatchers.IO) {
          if (validSession) {
            Logger.d(TAG, "Fetching user info using getUserUseCase")

            val result = getUserUseCase.execute(GetUserUseCase.Input(id = supabaseUser.id))
            val user = when (result) {
              is GetUserUseCase.Output.Success if result.user != null -> {
                if (result.user.role == UserRole.EMPLOYEE) {
                  result.user.copy(userInfo = supabaseUser)
                } else {
                  logout()
                  throw IllegalStateException("Invalid Admin Login on Employee App")
                }
              }

              is GetUserUseCase.Output.Failure -> {
                throw Exception(result.message)
              }

              else -> {
                logout()
                throw Exception("User data not found or inconsistent")
              }
            }

            _uiState.update {
              UIState.Ready(
                it.data?.copy(
                  hasValidSession = true, user = user, today = getTodayDate()
                ) ?: MainUiState(
                  hasValidSession = true, user = user, today = getTodayDate()
                )
              )
            }

            sessionManager.setCurrentEmployee(user)
          } else {
            sessionManager.clearSession()
          }
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Initialization failed: ${e.message}")
        sessionManager.clearSession()

        _uiState.update {
          UIState.Error(
            "Session Initialization Failed",
            data = it.data?.copy(hasValidSession = false, user = null, today = getTodayDate())
          )
        }
      }
    }
  }

  fun logout() {
    Logger.i(TAG, "Logging out...")
    _uiState.update {
      UIState.Loading(
        it.data?.copy(today = getTodayDate()) ?: MainUiState(today = getTodayDate())
      )
    }

    viewModelScope.launch {
      when (val result = logoutUseCase.execute(LogoutUseCase.Input())) {
        is LogoutUseCase.Output.Success -> {
          sessionManager.clearSession()
          _uiState.update {
            UIState.Ready(
              it.data?.copy(
                hasValidSession = false, user = null, today = getTodayDate()
              ) ?: MainUiState(
                hasValidSession = false, user = null, today = getTodayDate()
              )
            )
          }
          Logger.d(TAG, "Logout success")
        }

        is LogoutUseCase.Output.Failure -> {
          Logger.e(TAG, "Logout failed: ${result.message}")
          _uiState.update {
            UIState.Error("Error logging out", it.data?.copy(today = getTodayDate()))
          }
        }
      }
    }
  }

  private fun listenToUiState() {
    Logger.d(TAG, "Listening to UI Events")

    viewModelScope.launch(Dispatchers.Default) {
      uiState.collect {state ->
        when (state) {
          is UIState.Idle -> Logger.d(TAG, "UIState: Idle")
          is UIState.Loading -> Logger.d(TAG, "UIState: Loading")
          is UIState.Ready -> Logger.d(TAG, "UIState: Ready ${state.data}")
          is UIState.Error -> Logger.d(TAG, "UIState: Error ${state.message}")
        }
      }
    }
  }

  @OptIn(SupabaseExperimental::class)
  private fun listenToAuthEvents() {
    Logger.d(TAG, "Listening to AUTH Events")

    viewModelScope.launch(Dispatchers.Default) {
      auth.sessionStatus.collect { status ->
        when (status) {
          is SessionStatus.Authenticated -> {
            val token = auth.currentAccessTokenOrNull()
            Logger.d(
              TAG, """
                           Session source:${status.source}
                           SessionStatus: Authenticated
                           Access Token: $token
                           Session expiry:${status.session.expiresAt.toLocalDateTime(TimeZone.currentSystemDefault())}
                    """.trimIndent()
            )
            _toastMessageFlow.tryEmit("SessionStatus: Authenticated")

            // reinitialize session to load user data
            initializeSession()
          }

          is SessionStatus.NotAuthenticated -> {
            Logger.d(
              TAG, """
                           SessionStatus: NotAuthenticated
                           IsSignOut: ${status.isSignOut}
                    """.trimIndent()
            )
            _toastMessageFlow.tryEmit("SessionStatus: User is not authenticated")

            _uiState.update {
              UIState.Ready(
                it.data?.copy(
                  hasValidSession = false, user = null, today = getTodayDate()
                ) ?: MainUiState(
                  hasValidSession = false, user = null, today = getTodayDate()
                )
              )
            }
          }

          SessionStatus.Initializing -> {
            Logger.d(TAG, "Auth is initializing")
            _toastMessageFlow.tryEmit("SessionStatus: Auth is initializing")

            _uiState.update { UIState.Loading(it.data ?: MainUiState(today = getTodayDate())) }
          }

          is SessionStatus.RefreshFailure -> {
//            Logger.e(TAG, "Session refresh failed")
//            _toastMessageFlow.tryEmit("SessionStatus: Session refresh failed")
          }
        }
      }
    }

    viewModelScope.launch(Dispatchers.Default) {
      auth.events.collect { event ->
        when (event) {
          is AuthEvent.RefreshFailure -> {

            // update UI State
            when (val cause = event.cause) {
              is RefreshFailureCause.NetworkError -> _uiState.update {
                UIState.Error(
                  "Cannot connect to the server. Please check your internet connection",
                  it.data?.copy(hasValidSession = false, user = null, today = getTodayDate())
                )
              }

              is RefreshFailureCause.InternalServerError -> _uiState.update {
                UIState.Error(
                  "Internal Server Error",
                  it.data?.copy(hasValidSession = false, user = null, today = getTodayDate())
                )
              }
            }

            Logger.e(TAG, "AuthEvent: Session refresh failed: ${event.cause}")
            _toastMessageFlow.tryEmit("AuthEvent: Session refresh failed: ${event.cause}")
          }

          else -> {
            _uiState.update {
              UIState.Error(
                "Unknown Error",
                it.data?.copy(hasValidSession = false, user = null, today = getTodayDate())
              )
            }

            Logger.e(TAG, "AuthEvent: Unhandled event: $event")
            _toastMessageFlow.tryEmit("AuthEvent: Unhandled event: $event")
          }
        }
      }
    }
  }

  private fun getTodayDate(): LocalDateTime {
    val today = Clock.System.now().toLocalDateTime(TimeZone.of(Constants.TIMEZONE))
    return today
  }
}
