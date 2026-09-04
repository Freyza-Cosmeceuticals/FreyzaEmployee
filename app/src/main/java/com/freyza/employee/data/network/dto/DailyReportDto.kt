package com.freyza.employee.data.network.dto

import com.freyza.employee.core.util.BigDecimalSerializer
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.VisitType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

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

  @SerialName("travellingWithId")
  val travellingWithId: String?,

  @SerialName("ta")
  @Serializable(with = BigDecimalSerializer::class)
  val ta: BigDecimal?,
  @SerialName("da")
  @Serializable(with = BigDecimalSerializer::class)
  val da: BigDecimal?,
  @SerialName("totalExpense")
  @Serializable(with = BigDecimalSerializer::class)
  val totalExpense: BigDecimal?,

  @SerialName("visits")
  val visits: List<VisitDto>? = null,

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

@Serializable
data class VisitSummaryDto(
  @SerialName("reportId")
  val reportId: String,

  @SerialName("visitType")
  val visitType: VisitType,
)
