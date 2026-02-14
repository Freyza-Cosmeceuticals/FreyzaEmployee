package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.DayType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyReportDto(
  @SerialName("id")
  val id: String,

  @SerialName("employeeId")
  val employeeId: String,

  @SerialName("date")
  val date: String,

  @SerialName("dayType")
  val dayType: DayType,

  @SerialName("routeId")
  val routeId: String?,

  @SerialName("ta")
  val ta: Double?,
  @SerialName("da")
  val da: Double?,
  @SerialName("totalExpense")
  val totalExpense: Double?,

  @SerialName("locked")
  val locked: Boolean,
  @SerialName("lockedAt")
  val lockedAt: String?,

  @SerialName("createdAt")
  val createdAt: String,
  @SerialName("updatedAt")
  val updatedAt: String?,
)

@Serializable
data class DailyReportCreateDto(
  @SerialName("employeeId")
  val employeeId: String,

  @SerialName("date")
  val date: String,

  @SerialName("dayType")
  val dayType: DayType,

  @SerialName("routeId")
  val routeId: String?,
)
