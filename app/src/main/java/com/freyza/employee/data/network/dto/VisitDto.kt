package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.VisitType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisitDto(
  @SerialName("id")
  val id: String,

  @SerialName("reportId")
  val reportId: String,

  @SerialName("visitType")
  val visitType: VisitType,

  @SerialName("latitude")
  val latitude: Double,
  @SerialName("longitude")
  val longitude: Double,

  @SerialName("distanceMetersFromPOI")
  val distanceMetersFromPOI: Int,

  @SerialName("createdAt")
  val createdAt: String,
  @SerialName("updatedAt")
  val updatedAt: String?,
)

@Serializable
data class VisitCreateDto(
  @SerialName("reportId")
  val reportId: String,

  @SerialName("visitType")
  val visitType: VisitType,

  @SerialName("latitude")
  val latitude: Double,
  @SerialName("longitude")
  val longitude: Double,

  @SerialName("distanceMetersFromPOI")
  val distanceMetersFromPOI: Int,
)
