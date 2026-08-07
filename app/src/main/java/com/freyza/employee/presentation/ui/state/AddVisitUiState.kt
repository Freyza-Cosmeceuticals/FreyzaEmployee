package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Money
import com.freyza.employee.data.mappers.toProductDetail
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.VisitType
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class FormField(
  val value: String = "",
  val error: String? = null,
) {
  val isNotBlank: Boolean
    get() = value.isNotBlank()

  val isValidNumber: Boolean
    get() = value.toDoubleOrNull() != null
}

data class ProductEntry(
  val _id: String = UUID.randomUUID().toString(),
  val name: FormField = FormField(),
  val rate: FormField = FormField(),
  val quantity: FormField = FormField("1"),
) {
  val isValid: Boolean
    get() = name.isNotBlank && rate.isValidNumber && quantity.value.toIntOrNull() != null

  fun validate(): ProductEntry {
    return copy(
      name = name.copy(error = if (name.value.isBlank()) "Required" else null),
      rate = rate.copy(error = if (rate.value.toDoubleOrNull() == null) "Invalid" else null),
      quantity = quantity.copy(error = if (quantity.value.toIntOrNull() == null) "Invalid" else null)
    )
  }
}

data class AddVisitFormState(
  val name: FormField = FormField(),
  val poiId: String? = null,
  val notes: FormField = FormField(),
  val productEntries: List<ProductEntry> = emptyList(),
  val samplesGiven: List<String> = emptyList(),
  val orderTaken: Boolean = false,
  val billNo: FormField = FormField(),
  val paymentCollected: Boolean = false,
  val amountWithGST: FormField = FormField(),
  val amountWithoutGST: FormField = FormField(),
  val stockChecked: Boolean = false,
  val outstandingAmount: FormField = FormField(),
) {
  // Computed properties
  val numProducts: Int
    get() = productEntries.mapNotNull { it.toProductDetail() }.size

  val totalQuantity: Int
    get() = productEntries.mapNotNull { it.toProductDetail() }.sumOf { it.quantity }

  val orderAmount: Money
    get() = productEntries.mapNotNull { it.toProductDetail() }.fold(Money.ZERO) { acc, entry ->
      val rate = Money(entry.rate)
      val quantity = entry.quantity
      acc.plus(rate.times(quantity))
    }
}

data class AddVisitUiState(
  val today: LocalDateTime,
  val visitType: VisitType? = null,
  val availablePois: UIState<List<PointOfInterest>> = UIState.Idle(),
  val creationState: UIState<Boolean> = UIState.Idle(),
  val form: AddVisitFormState = AddVisitFormState(),
)

fun dummyAddVisitFormDoctor(): AddVisitFormState = AddVisitFormState(
  name = FormField("Dr. X"),
  notes = FormField("Very Nice Doctor"),
  orderTaken = true,
  productEntries = listOf(
    ProductEntry(
      name = FormField("Reactor"),
      rate = FormField("12.90"),
      quantity = FormField("56")
    ),
    ProductEntry(
      name = FormField("Capacitor"),
      rate = FormField("67.895"),
      quantity = FormField("62")
    ),
    ProductEntry(),
    ProductEntry(
      quantity = FormField("96")
    )
  ),
  samplesGiven = listOf("Conditioner", "Shampoo", "Facewash", "Lotion", "Octopus"),
  outstandingAmount = FormField("12892.27")
)

fun dummyAddVisitFormStockist(): AddVisitFormState = AddVisitFormState(
  name = FormField("Senku"),
  notes = FormField("Genius level Chemist"),
  samplesGiven = listOf("Sulfa Drugs", "GPS", "Medusa"),
  orderTaken = true,
  billNo = FormField("3718AD"),
  paymentCollected = true,
  amountWithGST = FormField("17652.23272"),
  amountWithoutGST = FormField("16273.27872"),
  outstandingAmount = FormField("999999.99"),
  stockChecked = true,
)

fun dummyAddVisitFormChemist(): AddVisitFormState = AddVisitFormState(
  name = FormField("Chemistry Man"),
  notes = FormField("Very Nice Chemist"),
  // should not show up
  samplesGiven = listOf("Detonator", "Nuclear Weapons", "TNT"),
  orderTaken = true,
  outstandingAmount = FormField("12892.27"),
)
