package com.freyza.employee.data.mappers

import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import kotlin.time.Instant

fun VisitDto.toDomain(): Visit {
  return when (visitType) {
    VisitType.DOCTOR -> Visit.DoctorVisit(
      id = id,
      reportId = reportId,
      employeeId = employeeId,
      latitude = latitude,
      longitude = longitude,
      distanceMetersFromPOI = distanceMetersFromPOI,
      doctorName = doctorName ?: "Unknown Doctor",
      productDetails = productDetails,
      samplesGiven = samplesGiven,
      orderTaken = orderTaken,
      orderAmount = orderAmount,
      outstandingAmount = outstandingAmount ?: 0.0,
      additionalNotes = additionalNotes,
      createdAt = Instant.parse(createdAt),
      updatedAt = updatedAt?.let { Instant.parse(it) })

    VisitType.STOCKIST -> Visit.StockistVisit(
      id = id,
      reportId = reportId,
      employeeId = employeeId,
      latitude = latitude,
      longitude = longitude,
      distanceMetersFromPOI = distanceMetersFromPOI,
      stockistName = stockistName ?: "Unknown Stockist",
      samplesGiven = samplesGiven,
      orderTaken = orderTaken,
      billNo = billNo ?: "N/A",
      paymentCollected = paymentCollected,
      amountWithGST = amountWithGST ?: 0.00,
      amountWithoutGST = amountWithoutGST ?: 0.00,
      outstandingAmount = outstandingAmount ?: 0.0,
      stockChecked = stockChecked,
      additionalNotes = additionalNotes,
      createdAt = Instant.parse(createdAt),
      updatedAt = updatedAt?.let { Instant.parse(it) })

    VisitType.CHEMIST -> Visit.ChemistVisit(
      id = id,
      reportId = reportId,
      employeeId = employeeId,
      latitude = latitude,
      longitude = longitude,
      distanceMetersFromPOI = distanceMetersFromPOI,
      chemistName = chemistName ?: "Unknown Chemist",
      orderTaken = orderTaken,
      outstandingAmount = outstandingAmount ?: 0.0,
      additionalNotes = additionalNotes,
      createdAt = Instant.parse(createdAt),
      updatedAt = updatedAt?.let { Instant.parse(it) })
  }
}

fun Visit.toDto(): VisitDto {
  return VisitDto(
    id = id,
    reportId = reportId,
    employeeId = employeeId,
    visitType = visitType,
    latitude = latitude,
    longitude = longitude,
    distanceMetersFromPOI = distanceMetersFromPOI,
    additionalNotes = additionalNotes,
    doctorName = (this as? Visit.DoctorVisit)?.doctorName,
    productDetails = (this as? Visit.DoctorVisit)?.productDetails ?: emptyList(),
    samplesGiven = when (this) {
      is Visit.DoctorVisit -> samplesGiven
      is Visit.StockistVisit -> samplesGiven
      else -> emptyList()
    },
    orderTaken = when (this) {
      is Visit.DoctorVisit -> orderTaken
      is Visit.StockistVisit -> orderTaken
      is Visit.ChemistVisit -> orderTaken
    },
    orderAmount = (this as? Visit.DoctorVisit)?.orderAmount,
    stockistName = (this as? Visit.StockistVisit)?.stockistName,
    billNo = (this as? Visit.StockistVisit)?.billNo,
    amountWithGST = (this as? Visit.StockistVisit)?.amountWithGST,
    amountWithoutGST = (this as? Visit.StockistVisit)?.amountWithoutGST,
    outstandingAmount = when (this) {
      is Visit.DoctorVisit -> outstandingAmount
      is Visit.StockistVisit -> outstandingAmount
      is Visit.ChemistVisit -> outstandingAmount
    },
    stockChecked = (this as? Visit.StockistVisit)?.stockChecked ?: false,
    paymentCollected = (this as? Visit.StockistVisit)?.paymentCollected ?: false,
    chemistName = (this as? Visit.ChemistVisit)?.chemistName,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}
