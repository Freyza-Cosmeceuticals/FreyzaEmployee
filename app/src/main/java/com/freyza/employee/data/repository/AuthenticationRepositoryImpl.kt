package com.freyza.employee.data.repository

import com.freyza.employee.core.AuthResponse
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import com.freyza.employee.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.jsonPrimitive

class AuthenticationRepositoryImpl(private val auth: Auth) : AuthenticationRepository {

    companion object {
        const val TAG = "AUTH_REPO"
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = auth.currentUserOrNull()
            if (user === null) {
                Logger.e(TAG, "Login Error: $email, Current user is null")
                return AuthResponse.Error("Unable to login")
            }

            val role = user.appMetadata?.get("app_role")?.jsonPrimitive?.content.toString()
            val status = user.appMetadata?.get("app_status")?.jsonPrimitive?.content.toString()

            if (role != UserRole.EMPLOYEE.toString()) {
                auth.signOut()
                throw Exception("Invalid admin login on Employee App")
            }

            if (status != UserStatus.ACTIVE.toString()) {
                auth.signOut()
                throw Exception("Inactive user cannot sign in")
            }

            AuthResponse.Success(user)

        } catch (e: Exception) {
            val cause = e.message?.lines()?.first().toString().trim()
            Logger.e(TAG, "Login Error: $email ${e.message.toString()}")
            AuthResponse.Error(cause)
        }
    }

    override suspend fun register(name: String, email: String, password: String): AuthResponse {
        return AuthResponse.Error("Registration not implemented in app")

//        return try {
//            auth.signUpWith(Email, "app://supabase.com/confirm") {
//                this.email = email
//                this.password = password
//                this.data = buildJsonObject {
//                    put("name", name)
//                }
//            }
//
//            AuthResponse.Success(auth.currentUserOrNull())
//        } catch (e: Exception) {
//            AuthResponse.Error(e.message.toString())
//        }
    }

    override suspend fun loginWithGoogle(): AuthResponse {
        return try {
            auth.signInWith(Google)
            val user = auth.currentUserOrNull()
            if (user === null) {
                Logger.e(TAG, "Login Error: Google, Current user is null")
                return AuthResponse.Error("Unable to login")
            }

            AuthResponse.Success(user)
        } catch (e: Exception) {
            AuthResponse.Error(e.message.toString())
        }
    }

    override suspend fun logout(): AuthResponse {
        return try {
            auth.signOut()
            AuthResponse.Logout
        } catch (e: Exception) {
            val cause = e.message?.lines()?.first().toString().trim()
            Logger.e(TAG, "Logout Error: ${e.message.toString()}")
            AuthResponse.Error(cause)
        }
    }

}
