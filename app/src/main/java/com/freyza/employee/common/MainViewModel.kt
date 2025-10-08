package com.freyza.employee.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.data.network.dto.UserDto
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

data class MainUiState(
    val isLoading: Boolean = true, val hasValidSession: Boolean = false, val user: UserDto? = null
)

const val TAG = "MainViewModel/AUTH"

class MainViewModel(private val supabaseClient: SupabaseClient) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    // SharedFlow to emit debug/toast messages.
    private val _toastMessageFlow = MutableSharedFlow<String>()
    val toastMessageFlow = _toastMessageFlow.asSharedFlow()


    init {
        viewModelScope.launch(Dispatchers.IO) {
            awaitSessionInit()

            val session = supabaseClient.auth.currentSessionOrNull()
            val user = supabaseClient.auth.currentUserOrNull()
            val validSession = session != null && user != null

            withContext(Dispatchers.Main) {
                if (validSession) {
                    val userDto = UserDto(
                        email = user.email ?: "N/A",
                        id = user.id,
                        name = (user.userMetadata?.get("name") ?: "N/A") as String
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false, hasValidSession = true, user = userDto
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false, hasValidSession = false, user = null
                        )
                    }
                }
            }
        }

        listenToAuthEvents()
    }

    private suspend fun awaitSessionInit() {
        supabaseClient.auth.sessionStatus.first { status ->
            status !is SessionStatus.Initializing
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
                        Logger.d(TAG, "Unhandled event: ${event.toString()}")
                        _toastMessageFlow.emit("Unhandled event: ${event.toString()}")
                    }
                }
            }
        }
    }
}
