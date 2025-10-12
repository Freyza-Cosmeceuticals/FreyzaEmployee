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
    val distance: Float,

    @SerialName("cost")
    val cost: Float,

    @SerialName("locked")
    var locked: Boolean = false
)
