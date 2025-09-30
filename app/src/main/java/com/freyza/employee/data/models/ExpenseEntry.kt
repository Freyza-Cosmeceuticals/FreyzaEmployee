package com.freyza.employee.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ExpenseEntry(
    @SerialName("id")
    val id: String,

    @SerialName("location")
    val location: String,

    @SerialName("distance")
    val distance: Float,

    @SerialName("cost")
    val cost: Float,

    @SerialName("locked")
    var locked: Boolean = false
)
