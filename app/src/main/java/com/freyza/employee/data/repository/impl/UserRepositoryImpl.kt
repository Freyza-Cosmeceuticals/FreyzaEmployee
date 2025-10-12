package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.domain.model.User
import com.freyza.employee.util.Logger
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class UserRepositoryImpl(private val postrest: Postgrest) : UserRepository {
    companion object {
        const val TAG: String = "UserRepositoryImpl"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getUserById(id: String): Result<User> {
        return try {
            withContext(Dispatchers.IO) {
                val userDto = postrest.from("users").select() {
                    filter {
                        UserDto::id eq id
                    }
                }.decodeSingleOrNull<UserDto>()

                val user = userDto?.let {
                    User(
                        id = it.id,
                        name = it.name,
                        role = it.role,
                        status = it.status,
                        location = it.location,
                        createdAt = Instant.parse(it.createdAt),
                        updatedAt = it.updatedAt?.let { Instant.parse(it) },
                        userInfo = null
                    )
                }

                Result.Success(user)
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}
