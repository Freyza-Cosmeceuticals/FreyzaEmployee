package com.freyza.employee.data.mappers

import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.toMoney
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.data.network.dto.VisitDto
import com.freyza.employee.data.network.dto.VisitUpdateDto
import com.freyza.employee.domain.model.ProductDetail
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.state.AddVisitFormState
import com.freyza.employee.presentation.ui.state.FormField
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
      poiId = poiId,
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
      poiId = poiId,
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
      poiId = poiId,
      orderTaken = orderTaken,
      paymentCollected = paymentCollected,
      amountWithGST = amountWithGST?.let { Money(it) } ?: Money.ZERO,
      amountWithoutGST = amountWithoutGST?.let { Money(it) } ?: Money.ZERO,
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
    poiId = poiId,
    additionalNotes = additionalNotes,
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
    billNo = (this as? Visit.StockistVisit)?.billNo,
    amountWithGST = when (this) {
      is Visit.StockistVisit -> amountWithGST.amount
      is Visit.ChemistVisit -> amountWithGST.amount
      else -> null
    },
    amountWithoutGST = when (this) {
      is Visit.StockistVisit -> amountWithoutGST.amount
      is Visit.ChemistVisit -> amountWithoutGST.amount
      else -> null
    },
    outstandingAmount = when (this) {
      is Visit.DoctorVisit -> outstandingAmount.amount
      is Visit.StockistVisit -> outstandingAmount.amount
      is Visit.ChemistVisit -> outstandingAmount.amount
    },
    stockChecked = (this as? Visit.StockistVisit)?.stockChecked ?: false,
    paymentCollected = when (this) {
      is Visit.StockistVisit -> paymentCollected
      is Visit.ChemistVisit -> paymentCollected
      else -> false
    },
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
  )
}

fun ProductEntry.toProductDetail(): ProductDetail? {
  if (!isValid) return null

  return ProductDetail(
    name = name.value,
    rate = rate.value.toMoney().amount,
    quantity = quantity.value.toIntOrNull() ?: 0
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
  visitType = visitType,
  latitude = latitude,
  longitude = longitude,

  poiId = poiId,
  newPoiName = if (poiId == null) name.value else null,

  productDetails = productEntries.mapNotNull {
    it.toProductDetail()
  },
  samplesGiven = samplesGiven,
  orderTaken = orderTaken,
  billNo = billNo.value,
  paymentCollected = paymentCollected,
  amountWithGST = amountWithGST.value.toMoney().amount,
  amountWithoutGST = amountWithoutGST.value.toMoney().amount,
  outstandingAmount = outstandingAmount.value.toMoney().amount,
  orderAmount = if (visitType == VisitType.DOCTOR) orderAmount.amount else null,
  stockChecked = stockChecked,
  additionalNotes = notes.value,
)

fun AddVisitFormState.toUpdateDto(visitType: VisitType, updatedAt: String? = null): VisitUpdateDto =
  VisitUpdateDto(
    poiId = poiId,
    newPoiName = if (poiId == null) name.value else null,
    productDetails = productEntries.mapNotNull { it.toProductDetail() },
    samplesGiven = samplesGiven,
    orderTaken = orderTaken,
    billNo = billNo.value.takeIf { visitType == VisitType.STOCKIST },
    paymentCollected = paymentCollected,
    amountWithGST = if (visitType == VisitType.STOCKIST || visitType == VisitType.CHEMIST) amountWithGST.value.toMoney().amount else null,
    amountWithoutGST = if (visitType == VisitType.STOCKIST || visitType == VisitType.CHEMIST) amountWithoutGST.value.toMoney().amount else null,
    outstandingAmount = outstandingAmount.value.toMoney().amount,
    orderAmount = if (visitType == VisitType.DOCTOR) orderAmount.amount else null,
    stockChecked = stockChecked,
    additionalNotes = notes.value,
    updatedAt = updatedAt
  )

fun Visit.toFormState(): AddVisitFormState {
  return AddVisitFormState(
    poiId = poiId,
    notes = FormField(additionalNotes ?: ""),
    productEntries = if (this is Visit.DoctorVisit) {
      productDetails.map { detail ->
        ProductEntry(
          name = FormField(detail.name),
          rate = FormField(detail.rate.toString()),
          quantity = FormField(detail.quantity.toString())
        )
      }
    } else emptyList(),
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
    billNo = FormField((this as? Visit.StockistVisit)?.billNo ?: ""),
    paymentCollected = when (this) {
      is Visit.StockistVisit -> paymentCollected
      is Visit.ChemistVisit -> paymentCollected
      else -> false
    },
    amountWithGST = FormField(
      when (this) {
        is Visit.StockistVisit -> amountWithGST.amount.toString()
        is Visit.ChemistVisit -> amountWithGST.amount.toString()
        else -> ""
      }
    ),
    amountWithoutGST = FormField(
      when (this) {
        is Visit.StockistVisit -> amountWithoutGST.amount.toString()
        is Visit.ChemistVisit -> amountWithoutGST.amount.toString()
        else -> ""
      }
    ),
    stockChecked = (this as? Visit.StockistVisit)?.stockChecked ?: false,
    outstandingAmount = when (this) {
      is Visit.DoctorVisit -> FormField(outstandingAmount.amount.toString())
      is Visit.StockistVisit -> FormField(outstandingAmount.amount.toString())
      is Visit.ChemistVisit -> FormField(outstandingAmount.amount.toString())
    }
  )
}
