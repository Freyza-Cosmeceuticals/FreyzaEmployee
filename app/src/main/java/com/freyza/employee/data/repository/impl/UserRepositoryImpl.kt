package com.freyza.employee.data.repository.impl

import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.util.Logger
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(private val postrest: Postgrest) : UserRepository {
    companion object {
        const val TAG: String = "UserRepositoryImpl"
    }

    override suspend fun getUserById(id: String): Result<UserDto> {
        return try {
            withContext(Dispatchers.IO) {
                val userDto = postrest.from("users").select() {
                    filter {
                        UserDto::id eq id
                    }
                }.decodeSingleOrNull<UserDto>()

                Result.Success(userDto)
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}
