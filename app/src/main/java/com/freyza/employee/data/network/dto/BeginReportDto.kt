package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.DayType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BeginReportRequest(
  @SerialName("date")
  val date: String,
  @SerialName("dayType")
  val dayType: DayType,
  @SerialName("routeId")
  val routeId: String?,
  @SerialName("travellingWithId")
  val travellingWithId: String?,
)

@Serializable
data class BeginReportResponse(
  @SerialName("success")
  val success: Boolean,
  @SerialName("data")
  val data: DailyReportDto
)
