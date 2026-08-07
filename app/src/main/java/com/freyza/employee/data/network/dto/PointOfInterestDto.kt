package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.VisitType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PointOfInterestDto(
  @SerialName("id")
  val id: String,

  @SerialName("name")
  val name: String,
  @SerialName("type")
  val type: VisitType,

  @SerialName("locationId")
  val locationId: String,

  @SerialName("latitude")
  val latitude: Double,
  @SerialName("longitude")
  val longitude: Double,

  @SerialName("createdAt")
  val createdAt: String,
  @SerialName("updatedAt")
  val updatedAt: String?,
)
