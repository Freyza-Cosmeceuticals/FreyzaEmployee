package com.freyza.employee.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    @SerialName("id")
    val id: String,

    @SerialName("srcLocId")
    val srcLocId: String,

    @SerialName("destLocId")
    val destLocId: String,

    @SerialName("distanceKm")
    val distanceKm: Float,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String?,
)
