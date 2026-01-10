package com.freyza.employee.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TravelPlanDto(
    @SerialName("id")
    val id: String,

    @SerialName("employeeId")
    val employeeId: String,

    @SerialName("month")
    val month: String,

    @SerialName("createdById")
    val createdById: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String?,
)
