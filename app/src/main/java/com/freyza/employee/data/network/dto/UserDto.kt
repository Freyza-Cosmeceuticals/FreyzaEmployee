package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.EmployeeTier
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

    @SerialName("email")
    val email: String,

    @SerialName("phone")
    val phone: String,

    @SerialName("role")
    val role: UserRole,

    @SerialName("status")
    val status: UserStatus,

    @SerialName("tier")
    val tier: EmployeeTier?,

    @SerialName("hqId")
    val hqId: String?,

    @SerialName("joiningDate")
    val joiningDate: String,

    @SerialName("resignDate")
    val resignDate: String?,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String?
)
