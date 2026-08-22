package com.freyza.employee.data.network.dto

import com.freyza.employee.core.util.BigDecimalSerializer
import com.freyza.employee.domain.model.ProductDetail
import com.freyza.employee.domain.model.VisitType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class VisitDto(
  @SerialName("id")
  val id: String,

  @SerialName("reportId")
  val reportId: String,
  @SerialName("employeeId")
  val employeeId: String,
  @SerialName("visitType")
  val visitType: VisitType,

  @SerialName("latitude")
  val latitude: Double,
  @SerialName("longitude")
  val longitude: Double,
  @SerialName("distanceMetersFromPOI")
  val distanceMetersFromPOI: Int,

  @SerialName("poiId")
  val poiId: String,

  @SerialName("productDetails")
  val productDetails: List<ProductDetail>,
  @SerialName("samplesGiven")
  val samplesGiven: List<String>,
  @SerialName("orderTaken")
  val orderTaken: Boolean,

  @SerialName("billNo")
  val billNo: String?,
  @SerialName("paymentCollected")
  val paymentCollected: Boolean,
  @SerialName("amountWithGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithGST: BigDecimal?,
  @SerialName("amountWithoutGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithoutGST: BigDecimal?,
  @SerialName("outstandingAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val outstandingAmount: BigDecimal?,
  @SerialName("orderAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val orderAmount: BigDecimal?,
  @SerialName("stockChecked")
  val stockChecked: Boolean,

  @SerialName("additionalNotes")
  val additionalNotes: String?,

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

  @SerialName("poiId")
  val poiId: String?,
  @SerialName("newPoiName")
  val newPoiName: String?,

  @SerialName("productDetails")
  val productDetails: List<ProductDetail>,
  @SerialName("samplesGiven")
  val samplesGiven: List<String>,
  @SerialName("orderTaken")
  val orderTaken: Boolean,

  @SerialName("billNo")
  val billNo: String?,
  @SerialName("paymentCollected")
  val paymentCollected: Boolean,
  @SerialName("amountWithGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithGST: BigDecimal?,
  @SerialName("amountWithoutGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithoutGST: BigDecimal?,
  @SerialName("outstandingAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val outstandingAmount: BigDecimal?,
  @SerialName("orderAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val orderAmount: BigDecimal?,
  @SerialName("stockChecked")
  val stockChecked: Boolean,

  @SerialName("additionalNotes")
  val additionalNotes: String?,
)

@Serializable
data class VisitUpdateDto(
  @SerialName("poiId")
  val poiId: String?,
  @SerialName("newPoiName")
  val newPoiName: String?,

  @SerialName("productDetails")
  val productDetails: List<ProductDetail>,
  @SerialName("samplesGiven")
  val samplesGiven: List<String>,
  @SerialName("orderTaken")
  val orderTaken: Boolean,

  @SerialName("billNo")
  val billNo: String?,
  @SerialName("paymentCollected")
  val paymentCollected: Boolean,
  @SerialName("amountWithGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithGST: BigDecimal?,
  @SerialName("amountWithoutGST")
  @Serializable(with = BigDecimalSerializer::class)
  val amountWithoutGST: BigDecimal?,
  @SerialName("outstandingAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val outstandingAmount: BigDecimal?,
  @SerialName("orderAmount")
  @Serializable(with = BigDecimalSerializer::class)
  val orderAmount: BigDecimal?,
  @SerialName("stockChecked")
  val stockChecked: Boolean,

  @SerialName("additionalNotes")
  val additionalNotes: String?,

  @SerialName("updatedAt")
  val updatedAt: String?,
)
