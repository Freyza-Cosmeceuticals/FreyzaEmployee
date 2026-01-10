package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.DayType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TravelPlanEntryDto(
    @SerialName("id")
    val id: String,

    @SerialName("tpId")
    val tpId: String,

    @SerialName("date")
    val date: String,

    @SerialName("dayType")
    val dayType: DayType,

    @SerialName("routeId")
    val routeId: String?,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String?,
)
