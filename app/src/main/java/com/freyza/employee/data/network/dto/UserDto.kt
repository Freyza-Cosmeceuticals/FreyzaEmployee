package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.UserRole
import com.freyza.employee.domain.model.UserStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

//    @SerialName("email")
//    val email: String,

    @SerialName("role")
    val role: UserRole,

    @SerialName("status")
    val status: UserStatus,

    @SerialName("location")
    val location: String?,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String?
)
