package com.freyza.employee._services

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.freyza.employee.BuildConfig
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.util._AuthResponse
import com.freyza.employee.util.SharedPreferencesHelper
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import java.util.UUID

class SupabaseAuthManager(private val context: Context) : AuthManager {
    companion object {
        private const val ACCESS_TOKEN_KEY: String = "accessToken"
    }

    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    override suspend fun loginWithEmail(
        emailValue: String,
        passwordValue: String
    ): Flow<_AuthResponse> = flow {
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            saveToken()

            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo == null) {
                emit(_AuthResponse.Error("User not found"))
            } else {
                emit(
                    _AuthResponse.Success(
                        UserDto(
                            id = userInfo.id,
                            name = userInfo.email.toString(),
                            email = userInfo.email.toString()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(_AuthResponse.Error(e.message ?: ""))
        }
    }

    override suspend fun loginWithGoogle(): Flow<_AuthResponse> = flow {
        val hashedNonce = createNonce()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        try {
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            val googleIdToken = googleIdTokenCredential.idToken

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            saveToken()

            val userInfo = supabase.auth.currentUserOrNull()
            if (userInfo == null) {
                emit(_AuthResponse.Error("User not found"))
            } else {
                emit(
                    _AuthResponse.Success(
                        UserDto(
                            id = userInfo.id,
                            name = userInfo.email.toString(),
                            email = userInfo.email.toString()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(_AuthResponse.Error(e.message ?: ""))
        }
    }

    override suspend fun logout(): Flow<_AuthResponse> = flow {
        try {
            val credentialManager = CredentialManager.create(context)
//            credentialManager.clearCredentialState(ClearCredentialStateRequest.TYPE_CLEAR_CREDENTIAL_STATE)
            supabase.auth.signOut()
            revokeToken()

            emit(_AuthResponse.Success(null))
        } catch (e: Exception) {
            emit(_AuthResponse.Error(e.message ?: ""))
        }
    }

    override suspend fun isLoggedIn(): Flow<_AuthResponse> = flow {
        try {
            val token = getToken()
            
            if (token.isNullOrEmpty()) {
                emit(_AuthResponse.Error("User is not logged in"))
            } else {
                val user = supabase.auth.retrieveUser(token)
                supabase.auth.refreshCurrentSession()
                saveToken()
                emit(
                    _AuthResponse.Success(
                        UserDto(
                            user.id,
                            user.email.toString(),
                            user.email.toString()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(_AuthResponse.Error(e.message.toString()))
        }
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    private fun saveToken() {
        val accessToken = supabase.auth.currentAccessTokenOrNull()
        val sharedPref = SharedPreferencesHelper(context)
        sharedPref.saveStringData(ACCESS_TOKEN_KEY, accessToken)
    }

    private fun revokeToken() {
        val sharedPref = SharedPreferencesHelper(context)
        sharedPref.removeStringData(ACCESS_TOKEN_KEY)
    }

    private fun getToken(): String? {
        val sharedPref = SharedPreferencesHelper(context)
        return sharedPref.getStringData(ACCESS_TOKEN_KEY)
    }
}
