package com.freyza.employee.data.repository

import com.freyza.employee.core.Result
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class UserRepositoryImpl(
    private val auth: Auth,
    private val postgres: Postgrest,
) : UserRepository {
    companion object {
        const val TAG: String = "USER_REPO"
    }

    override suspend fun getUserById(id: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val userDto = postgres.from("user").select {
                    filter {
                        UserDto::id eq id
                    }
                }.decodeSingleOrNull<UserDto>()

                val user = userDto?.let { u ->
                    User(
                        id = u.id,
                        name = u.name,
                        email = u.email,
                        phone = u.phone,
                        role = u.role,
                        tier = u.tier,
                        status = u.status,
                        hqId = u.hqId,
                        joiningDate = LocalDate.parse(u.joiningDate),
                        resignDate = u.resignDate?.let { LocalDate.parse(it) },
                        createdAt = Instant.parse(u.createdAt),
                        updatedAt = u.updatedAt?.let { Instant.parse(it) },
                        userInfo = null,
                    )
                }

                Result.Success(user)
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getCurrentUser(): Result<UserInfo> {
        return try {
            val userInfo = auth.currentUserOrNull()
            Result.Success(userInfo)
        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}
