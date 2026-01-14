package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import com.freyza.employee.presentation.ui.state.MainUiState
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

const val TAG = "MAIN_VIEW_MODEL"

class MainViewModel(
    private val auth: Auth, val getUserUseCase: GetUserUseCase, val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState<MainUiState>>(UIState.Idle())
    val uiState = _uiState.asStateFlow()

    // SharedFlow to emit debug/toast messages.
    private val _toastMessageFlow = MutableSharedFlow<String>()
    val toastMessageFlow = _toastMessageFlow.asSharedFlow()


    init {
        initializeSession()
        listenToAuthEvents()
    }

    private suspend fun awaitSessionInit() {
        auth.sessionStatus.first { status ->
            status !is SessionStatus.Initializing
        }
    }

    fun initializeSession() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UIState.Loading()

            try {
                awaitSessionInit()

                val session = auth.currentSessionOrNull()
                val supabaseUser = auth.currentUserOrNull()
                val validSession = session != null && supabaseUser != null

                withContext(Dispatchers.Main) {
                    if (validSession) {
                        Logger.d(TAG, "Fetching user info using getUserUseCase")

                        val result =
                            getUserUseCase.execute(GetUserUseCase.Input(id = supabaseUser.id))

                        val user = when (result) {
                            is GetUserUseCase.Output.Success if result.user != null -> {
                                if (result.user.role == UserRole.EMPLOYEE) {
                                    result.user.copy(userInfo = supabaseUser)
                                } else {
                                    Logger.e(TAG, "Error: Invalid Admin Login on Employee App")
                                    logout()
                                    throw Exception("Invalid admin login on Employee App")
                                }
                            }

                            is GetUserUseCase.Output.Failure -> {
                                Logger.e(TAG, "Error: ${result.message}")
                                throw Exception(result.message)
                            }

                            else -> {
                                null
                            }
                        }

                        _uiState.update {
                            UIState.Ready(
                                it.data?.copy(
                                    hasValidSession = true,
                                    user = user,
                                    today = getTodayDate()
                                ) ?: MainUiState(
                                    hasValidSession = true, user = user, today = getTodayDate()
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            UIState.Ready(
                                it.data?.copy(
                                    hasValidSession = false,
                                    user = null,
                                    today = getTodayDate()
                                )
                                    ?: MainUiState(
                                        hasValidSession = false, user = null, getTodayDate()
                                    )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    UIState.Error(
                        e.message.toString(),
                        data = it.data?.copy(
                            hasValidSession = false,
                            user = null,
                            today = getTodayDate()
                        )
                    )
                }
            }
        }
    }

    private fun getTodayDate(): LocalDateTime {
        val today = Clock.System.now().toLocalDateTime(TimeZone.of(Constants.TIMEZONE))
        return today
    }

    fun logout() {
        _uiState.value = UIState.Loading()

        viewModelScope.launch {
            when (val result = logoutUseCase.execute(LogoutUseCase.Input())) {
                is LogoutUseCase.Output.Success -> {
                    _uiState.update {
                        UIState.Ready(
                            it.data?.copy(
                                hasValidSession = false,
                                user = null,
                                today = getTodayDate()
                            ) ?: MainUiState(
                                hasValidSession = false,
                                user = null, today = getTodayDate()
                            )
                        )
                    }
                    Logger.d("AUTH", "Logout success")
                }

                is LogoutUseCase.Output.Failure -> {
                    _uiState.update {
                        UIState.Error("Error logging out", it.data)
                    }
                    Logger.d("AUTH", "Logout failed ${result.message}")
                }
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    private fun listenToAuthEvents() {
        viewModelScope.launch(Dispatchers.IO) {
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
                        _toastMessageFlow.emit("Authenticated")
                    }

                    is SessionStatus.NotAuthenticated -> {
                        Logger.d(
                            TAG, """
                           SessionStatus: NotAuthenticated
                           IsSignOut: ${status.isSignOut}
                    """.trimIndent()
                        )
                        _toastMessageFlow.emit("User is not authenticated")
                    }

                    SessionStatus.Initializing -> {
                        Logger.d(TAG, "Auth is initializing")
                        _toastMessageFlow.emit("Auth is initializing")
                    }

                    is SessionStatus.RefreshFailure -> {
                        Logger.d(TAG, "Session refresh failed")
                        _toastMessageFlow.emit("Session refresh failed")
                    }

                }
            }
        }

        viewModelScope.launch {
            auth.events.collect { event ->
                when (event) {
                    is AuthEvent.RefreshFailure -> {
                        Logger.d(TAG, "Session refresh failed: ${event.cause}")
                        _toastMessageFlow.emit("Session refresh failed: ${event.cause}")
                    }

                    else -> {
                        Logger.d(TAG, "Unhandled event: $event")
                        _toastMessageFlow.emit("Unhandled event: ${event}")
                    }
                }
            }
        }
    }
}
