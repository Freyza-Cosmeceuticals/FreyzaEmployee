package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.toMoney
import com.freyza.employee.data.mappers.toProductDetail
import com.freyza.employee.domain.model.VisitType
import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class ProductEntry(
  val id: String = UUID.randomUUID().toString(),
  val name: String = "",
  val rate: String = "",
  val quantity: String = "1",
)

data class AddVisitFormState(
  val doctorName: String = "",
  val stockistName: String = "",
  val chemistName: String = "",
  val notes: String = "",

  val productEntries: List<ProductEntry> = emptyList(),
  val samplesGiven: List<String> = emptyList(),
  val orderTaken: Boolean = false,

  val billNo: String = "",
  val paymentCollected: Boolean = false,
  val amountWithGST: String = "",
  val amountWithoutGST: String = "",
  val stockChecked: Boolean = false,
  val outstandingAmount: String = "",
) {
  // Computed property: Calculates total automatically whenever productEntries changes
  val orderAmount: Money
    get() = productEntries.mapNotNull { it.toProductDetail() }.fold(Money.ZERO) { acc, entry ->
      val rate = entry.rate.toPlainString().toMoney()
      val quantity = entry.quantity
      acc.plus(rate.times(quantity))
    }
}

data class AddVisitUiState(
  val today: LocalDateTime,
  val visitType: VisitType? = null,
  val creationState: UIState<Boolean> = UIState.Idle(),
  val form: AddVisitFormState = AddVisitFormState(),
)

fun dummyAddVisitFormDoctor(): AddVisitFormState = AddVisitFormState(
  doctorName = "Dr. X",
  notes = "Very Nice Doctor",
  orderTaken = true,
  productEntries = listOf(
    ProductEntry(
      name = "Reactor",
      rate = "12.90",
      quantity = "56"
    ),
    ProductEntry(
      name = "Capacitor",
      rate = "67.895",
      quantity = "62"
    ),
    ProductEntry(),
    ProductEntry(
      quantity = "96"
    )
  ),
  samplesGiven = listOf("Conditioner", "Shampoo", "Facewash", "Lotion", "Octopus"),
  outstandingAmount = "12892.27"
)

fun dummyAddVisitFormStockist(): AddVisitFormState = AddVisitFormState(
  stockistName = "Senku",
  notes = "Genius level Chemist",
  samplesGiven = listOf("Sulfa Drugs", "GPS", "Medusa"),
  orderTaken = true,
  billNo = "5173AD",
  paymentCollected = true,
  amountWithGST = "17652.23272",
  amountWithoutGST = "16273.27872",
  outstandingAmount = "999999.99",
  stockChecked = true,
)

fun dummyAddVisitFormChemist(): AddVisitFormState = AddVisitFormState(
  chemistName = "Chemistry Man",
  notes = "Very Nice Chemist",
  // should not show up
  samplesGiven = listOf("Detonator", "Nuclear Weapons", "TNT"),
  orderTaken = true,
  outstandingAmount = "12892.27",
)
