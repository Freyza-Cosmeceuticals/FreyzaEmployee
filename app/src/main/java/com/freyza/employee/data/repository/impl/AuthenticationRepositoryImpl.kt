package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.AuthResponse
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.jsonPrimitive

private const val logTag = "AuthenticationRepository"

class AuthenticationRepositoryImpl(private val auth: Auth) : AuthenticationRepository {

    override suspend fun login(email: String, password: String): AuthResponse {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = auth.currentUserOrNull()
            val role =
                user?.userMetadata?.get("role")?.jsonPrimitive?.content.toString()
            val status =
                user?.userMetadata?.get("status")?.jsonPrimitive?.content.toString()

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
            AuthResponse.Error(e.message.toString())
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
            AuthResponse.Success(auth.currentUserOrNull())
        } catch (e: Exception) {
            AuthResponse.Error(e.message.toString())
        }
    }

    override suspend fun logout(): AuthResponse {
        return try {
            auth.signOut()
            AuthResponse.Success(null)
        } catch (e: Exception) {
            AuthResponse.Error(e.message.toString())
        }
    }

}
