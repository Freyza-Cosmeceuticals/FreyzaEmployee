package com.freyza.employee.data.network.dto

import com.freyza.employee.domain.model.TravelPlanMetrics
import kotlinx.serialization.Serializable

@Serializable
data class TravelPlanMetricsResponse(
  val success: Boolean,
  val data: TravelPlanMetricsDto,
)

@Serializable
data class TravelPlanMetricsDto(
  val targetAmount: Double,
  val employeeId: String,
  val totalOrderAmount: Double,
  val totalAmountWithoutGST: Double,
)

fun TravelPlanMetricsDto.toDomain(): TravelPlanMetrics = TravelPlanMetrics(
  targetAmount = targetAmount,
  employeeId = employeeId,
  totalOrderAmount = totalOrderAmount,
  totalAmountWithoutGST = totalAmountWithoutGST
)
