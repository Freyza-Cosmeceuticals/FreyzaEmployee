package com.freyza.employee.data.mappers

import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.toMoney
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.domain.model.ProductDetail
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.state.AddVisitFormState
import com.freyza.employee.presentation.ui.state.ProductEntry
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
      orderAmount = orderAmount?.let { Money(it) },
      outstandingAmount = outstandingAmount?.let { Money(it) } ?: Money.ZERO,
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
      amountWithGST = amountWithGST?.let { Money(it) } ?: Money.ZERO,
      amountWithoutGST = amountWithoutGST?.let { Money(it) } ?: Money.ZERO,
      outstandingAmount = outstandingAmount?.let { Money(it) } ?: Money.ZERO,
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
      outstandingAmount = outstandingAmount?.let { Money(it) } ?: Money.ZERO,
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
    orderAmount = (this as? Visit.DoctorVisit)?.orderAmount?.amount,
    stockistName = (this as? Visit.StockistVisit)?.stockistName,
    billNo = (this as? Visit.StockistVisit)?.billNo,
    amountWithGST = (this as? Visit.StockistVisit)?.amountWithGST?.amount,
    amountWithoutGST = (this as? Visit.StockistVisit)?.amountWithoutGST?.amount,
    outstandingAmount = when (this) {
      is Visit.DoctorVisit -> outstandingAmount.amount
      is Visit.StockistVisit -> outstandingAmount.amount
      is Visit.ChemistVisit -> outstandingAmount.amount
    },
    stockChecked = (this as? Visit.StockistVisit)?.stockChecked ?: false,
    paymentCollected = (this as? Visit.StockistVisit)?.paymentCollected ?: false,
    chemistName = (this as? Visit.ChemistVisit)?.chemistName,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}

fun ProductEntry.toProductDetail(): ProductDetail? {
  if (name.isBlank() || rate.toMoney() == Money.ZERO || quantity.toIntOrNull() == null) {
    return null
  }

  return ProductDetail(
    name = name, rate = rate.toMoney().amount, quantity = quantity.toIntOrNull() ?: 0
  )
}

fun AddVisitFormState.toDto(
  reportId: String,
  employeeId: String,
  visitType: VisitType,
  latitude: Double,
  longitude: Double,
): VisitCreateDto = VisitCreateDto(
  reportId = reportId,
  employeeId = employeeId,
  visitType = visitType,
  latitude = latitude,
  longitude = longitude,
  distanceMetersFromPOI = 0,

  doctorName = doctorName.takeIf { visitType == VisitType.DOCTOR },
  chemistName = chemistName.takeIf { visitType == VisitType.CHEMIST },
  stockistName = stockistName.takeIf { visitType == VisitType.STOCKIST },
  productDetails = productEntries.mapNotNull {
    it.toProductDetail()
  },
  samplesGiven = samplesGiven,
  orderTaken = orderTaken,
  billNo = billNo,
  paymentCollected = paymentCollected,
  amountWithGST = amountWithGST.toMoney().amount,
  amountWithoutGST = amountWithoutGST.toMoney().amount,
  outstandingAmount = outstandingAmount.toMoney().amount,
  orderAmount = if (visitType == VisitType.DOCTOR) orderAmount.amount else null,
  stockChecked = stockChecked,
  additionalNotes = notes,
)
