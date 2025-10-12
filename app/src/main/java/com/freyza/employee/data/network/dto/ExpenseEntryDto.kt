package com.freyza.employee.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseEntryDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("location")
    val location: String,

    @SerialName("distance")
    val distance: Double,

    @SerialName("cost")
    val cost: Double,

    @SerialName("locked")
    var locked: Boolean = false
)
