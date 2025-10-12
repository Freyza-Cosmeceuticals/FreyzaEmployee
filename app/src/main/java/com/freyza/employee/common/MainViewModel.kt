package com.freyza.employee.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.domain.model.MainUiState
import com.freyza.employee.domain.usecase.auth.GetUserUseCase
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.auth
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

const val TAG = "MainViewModel/AUTH"

@OptIn(ExperimentalTime::class)
class MainViewModel(
    private val supabaseClient: SupabaseClient,
    val getUserUseCase: GetUserUseCase,
    val logoutUseCase: LogoutUseCase
) :
    ViewModel() {
    private val _uiState = MutableStateFlow<Result<MainUiState>>(Result.Loading(MainUiState()))
    val uiState = _uiState.asStateFlow()

    // SharedFlow to emit debug/toast messages.
    private val _toastMessageFlow = MutableSharedFlow<String>()
    val toastMessageFlow = _toastMessageFlow.asSharedFlow()


    init {
        initializeSession()
        listenToAuthEvents()
    }


    private suspend fun awaitSessionInit() {
        supabaseClient.auth.sessionStatus.first { status ->
            status !is SessionStatus.Initializing
        }
    }

    fun initializeSession() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                awaitSessionInit()

                val session = supabaseClient.auth.currentSessionOrNull()
                val supabaseUser = supabaseClient.auth.currentUserOrNull()
                val validSession = session != null && supabaseUser != null

                withContext(Dispatchers.Main) {
                    if (validSession) {
                        Logger.d(TAG, "Fetching user info using getUserUseCase")

                        val result =
                            getUserUseCase.execute(GetUserUseCase.Input(id = supabaseUser.id))

                        val user = when {
                            result is GetUserUseCase.Output.Success && result.user != null -> {
                                result.user.copy(userInfo = supabaseUser)
                            }

                            result is GetUserUseCase.Output.Failure -> {
                                Logger.e(TAG, "Error: ${result.message}")
                                throw Exception(result.message)
                            }

                            else -> {
                                null
                            }
                        }

                        _uiState.update {
                            Result.Success(
                                it.data?.copy(
                                    hasValidSession = true, user = user
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            Result.Success(
                                it.data?.copy(
                                    hasValidSession = false, user = null
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    Result.Error(
                        e.message.toString(),
                        data = it.data?.copy(hasValidSession = false, user = null)
                    )
                }
            }
        }
    }

    fun logout() {
        _uiState.value = Result.Loading()

        viewModelScope.launch {
            val result = logoutUseCase.execute(LogoutUseCase.Input())

            when (result) {
                is LogoutUseCase.Output.Success -> {
                    _uiState.update {
                        Result.Success(MainUiState(hasValidSession = false, user = null))
                    }
                    Logger.d("AUTH", "Logout success")
                }

                is LogoutUseCase.Output.Failure -> {
                    _uiState.update {
                        Result.Error("Error logging out")
                    }
                    Logger.d("AUTH", "Logout failed ${result.message}")
                }
            }
        }
    }

    @OptIn(SupabaseExperimental::class, ExperimentalTime::class)
    private fun listenToAuthEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val token = supabaseClient.auth.currentAccessTokenOrNull()
                        Logger.d(
                            TAG, """
                           Session source:${status.source}
                           SessionStatus: Authenticated
                           Access Token: $token
                           Session expiry:${status.session.expiresAt.toLocalDateTime(TimeZone.currentSystemDefault())}
                    """
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
            supabaseClient.auth.events.collect { event ->
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
