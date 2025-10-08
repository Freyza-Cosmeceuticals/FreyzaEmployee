package com.freyza.employee.data.repository.impl

import android.util.Log
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.model.AuthState
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.ExperimentalTime

private const val logTag = "AuthenticationRepository"

class AuthenticationRepositoryImpl(private val auth: Auth) : AuthenticationRepository {

    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Initializing)
    override val authState: StateFlow<AuthState> = _authState

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                logSessionStatus(status)
            }
        }
    }

    override suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            true
        } catch (e: Exception) {
            // TODO: Return actual error messages
            false
        }
    }

    override suspend fun register(name: String, email: String, password: String): Boolean {
        return try {
            auth.signUpWith(Email, "app://supabase.com/confirm") {
                this.email = email
                this.password = password
                this.data = JsonObject(mapOf("name" to JsonPrimitive(name)))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loginWithGoogle(): Boolean {
        return try {
            auth.signInWith(Google)
            true
        } catch (e: Exception) {
            false
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun logSessionStatus(sessionStatus: SessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                Log.d(
                    logTag, """
                           Session source:${sessionStatus.source}
                           SessionStatus: Authenticated
                           Session expiry:${sessionStatus.session.expiresAt.toLocalDateTime(TimeZone.UTC)}
                    """
                )
                _authState.value = AuthState.Authenticated
            }


            SessionStatus.Initializing -> {
                Log.d(logTag, "SessionStatus: Initializing")
                _authState.value = AuthState.Initializing

            }


            is SessionStatus.RefreshFailure -> {
                Log.d(logTag, "SessionStatus: RefreshFailure")
            }

            is SessionStatus.NotAuthenticated -> {
                Log.d(
                    logTag,
                    """
                           SessionStatus: NotAuthenticated
                           IsSignOut: ${sessionStatus.isSignOut}
                    """.trimIndent()
                )
                _authState.value = AuthState.Unauthenticated
            }

        }
    }

    override suspend fun exchangeCodeForSession(code: String): Result<Unit> =
        runCatching {
            auth.exchangeCodeForSession(code = code, saveSession = true)
            return Result.success(Unit)
        }.onFailure {
            return Result.failure(it)
        }

    override suspend fun verifyEmail(tokenHash: String): Result<Unit> = runCatching {
        auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,
            tokenHash = tokenHash
        )
        return Result.success(Unit)
    }.onFailure { e ->
        return Result.failure(e)
    }

    override suspend fun logout(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

}
