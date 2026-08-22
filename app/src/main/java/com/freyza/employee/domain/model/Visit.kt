package com.freyza.employee.domain.model

import androidx.annotation.Keep
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.freyza.employee.R
import com.freyza.employee.core.util.BigDecimalSerializer
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.times
import com.freyza.employee.core.util.toMoney
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Instant

@Serializable
data class ProductDetail(
  val name: String,
  @Serializable(with = BigDecimalSerializer::class)
  val rate: BigDecimal,
  val quantity: Int,
) {
  val total: BigDecimal get() = rate.multiply(BigDecimal(quantity))
}

sealed class Visit {
  abstract val id: String
  abstract val reportId: String
  abstract val employeeId: String
  abstract val visitType: VisitType

  abstract val latitude: Double
  abstract val longitude: Double
  abstract val distanceMetersFromPOI: Int

  abstract val poiId: String

  abstract val additionalNotes: String?

  abstract val createdAt: Instant
  abstract val updatedAt: Instant?

  data class DoctorVisit(
    override val id: String,
    override val reportId: String,
    override val employeeId: String,

    override val latitude: Double,
    override val longitude: Double,
    override val distanceMetersFromPOI: Int,

    override val poiId: String,

    val productDetails: List<ProductDetail> = emptyList(),
    val samplesGiven: List<String> = emptyList(),
    val orderTaken: Boolean = false,
    val orderAmount: Money? = null,
    val outstandingAmount: Money = Money.ZERO,

    override val additionalNotes: String?,

    override val createdAt: Instant,
    override val updatedAt: Instant?,
  ) : Visit() {
    override val visitType = VisitType.DOCTOR
  }

  data class StockistVisit(
    override val id: String,
    override val reportId: String,
    override val employeeId: String,

    override val latitude: Double,
    override val longitude: Double,
    override val distanceMetersFromPOI: Int,

    override val poiId: String,

    val samplesGiven: List<String> = emptyList(),
    val orderTaken: Boolean = false,
    val billNo: String,
    val paymentCollected: Boolean,
    val amountWithGST: Money,
    val amountWithoutGST: Money,
    val outstandingAmount: Money = Money.ZERO,
    val stockChecked: Boolean = false,

    override val additionalNotes: String?,

    override val createdAt: Instant,
    override val updatedAt: Instant?,
  ) : Visit() {
    override val visitType = VisitType.STOCKIST
  }

  data class ChemistVisit(
    override val id: String,
    override val reportId: String,
    override val employeeId: String,

    override val latitude: Double,
    override val longitude: Double,
    override val distanceMetersFromPOI: Int,

    override val poiId: String,

    val orderTaken: Boolean = false,
    val outstandingAmount: Money = Money.ZERO,

    override val additionalNotes: String?,

    override val createdAt: Instant,
    override val updatedAt: Instant?,
  ) : Visit() {
    override val visitType: VisitType = VisitType.CHEMIST
  }
}

data class VisitCreate(
  val poiId: String?,
  val newPoiName: String?,
  val productDetails: List<ProductDetail>,
  val samplesGiven: List<String>,
  val orderTaken: Boolean,
  val billNo: String?,
  val paymentCollected: Boolean,
  val amountWithGST: Money,
  val amountWithoutGST: Money,
  val outstandingAmount: Money,
  val orderAmount: Money?,
  val stockChecked: Boolean,
  val notes: String?,
)

@Keep
@Serializable
enum class VisitType {
  DOCTOR, STOCKIST, CHEMIST;

  fun titleCase(): String =
    this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)

  fun iconResource(): Int {
    return when (this) {
      DOCTOR -> R.drawable.stethoscope_24px
      STOCKIST -> R.drawable.inventory_2_24px
      CHEMIST -> R.drawable.labs_24px
    }
  }
}

fun dummyVisitDoctor(): Visit = Visit.DoctorVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "313dcc3f-2a97-4151-bdcc-3f2a97815108",
  latitude = 34.632,
  longitude = 55.246,
  distanceMetersFromPOI = 55,
  poiId = "8f8a31e5-d5b4-453e-8a31-e5d5b4d53e4b",
  productDetails = if (Random.nextBoolean()) listOf(
    ProductDetail("Generator", BigDecimal("100.00"), 1),
    ProductDetail("Repulsor", BigDecimal("200.00"), 2),
    ProductDetail("Reactor", BigDecimal("300.00"), 1)
  ) else emptyList(),
  samplesGiven = if (Random.nextBoolean()) listOf("Capacitor", "Inductor").times(
    Random.nextInt(
      1, 3
    )
  ) else emptyList(),
  orderTaken = Random.nextBoolean(),
  orderAmount = if (Random.nextBoolean()) "1000.00".toMoney() else null,
  outstandingAmount = Random.nextDouble(0.0, 1000.0).toMoney(),
  additionalNotes = if (Random.nextBoolean()) "Doctor was a genius..." else null,
  createdAt = Instant.parse("2026-02-11T21:32:38.409+05:30"),
  updatedAt = null,
)

fun dummyVisitDoctorAllTrue(): Visit = Visit.DoctorVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "313dcc3f-2a97-4151-bdcc-3f2a97815108",
  latitude = 34.632,
  longitude = 55.246,
  distanceMetersFromPOI = 55,
  poiId = "8f8a31e5-d5b4-453e-8a31-e5d5b4d53e4b",
  productDetails = listOf(
    ProductDetail("Generator", BigDecimal("100.00"), 1),
    ProductDetail("Repulsor", BigDecimal("200.00"), 2),
    ProductDetail("Reactor", BigDecimal("300.00"), 1)
  ),
  samplesGiven = listOf("Capacitor", "Inductor").times(Random.nextInt(1, 3)),
  orderTaken = true,
  orderAmount = "1000.00".toMoney(),
  outstandingAmount = Random.nextDouble(0.0, 1000.0).toMoney(),
  additionalNotes = "Doctor was a genius...",
  createdAt = Instant.parse("2026-02-11T21:32:38.409+05:30"),
  updatedAt = null,
)

fun dummyVisitChemist(): Visit = Visit.ChemistVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "1677545b-7aa7-4f8a-b754-5b7aa7df8a95",
  latitude = 45.653,
  longitude = 22.216,
  distanceMetersFromPOI = 34,
  poiId = "91367409-f38a-4023-b674-09f38a80236f",
  orderTaken = Random.nextBoolean(),
  outstandingAmount = Random.nextDouble(0.0, 1000.0).toMoney(),
  additionalNotes = if (Random.nextBoolean()) "Chemist was good" else null,
  createdAt = Instant.parse("2026-02-11T21:33:28.453+05:30"),
  updatedAt = null
)

fun dummyVisitChemistAllTrue(): Visit = Visit.ChemistVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "1677545b-7aa7-4f8a-b754-5b7aa7df8a95",
  latitude = 45.653,
  longitude = 22.216,
  distanceMetersFromPOI = 34,
  poiId = "91367409-f38a-4023-b674-09f38a80236f",
  orderTaken = true,
  outstandingAmount = "500.00".toMoney(),
  additionalNotes = "",
  createdAt = Instant.parse("2026-02-11T21:33:28.453+05:30"),
  updatedAt = null
)

fun dummyVisitStockist(): Visit = Visit.StockistVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "f2c2561f-4df8-4894-8256-1f4df8c894f4",
  latitude = 45.367,
  longitude = 43.326,
  distanceMetersFromPOI = 65,
  poiId = "9e54e0bd-0bb7-4a49-94e0-bd0bb72a49cc",
  billNo = "Bill 222345",
  paymentCollected = Random.nextBoolean(),
  amountWithGST = Random.nextDouble(300.00, 500.00).toMoney(),
  amountWithoutGST = Random.nextDouble(100.00, 300.00).toMoney(),
  outstandingAmount = Random.nextDouble(0.0, 1000.0).toMoney(),
  stockChecked = Random.nextBoolean(),
  samplesGiven = if (Random.nextBoolean()) listOf("Capacitor", "Inductor").times(
    Random.nextInt(
      1, 3
    )
  ) else emptyList(),
  orderTaken = Random.nextBoolean(),
  additionalNotes = if (Random.nextBoolean()) "Stockist is well... a stockist who stocks things permanently, well, kind of..." else null,
  createdAt = Instant.parse("2026-02-11T21:34:04.734+05:30"),
  updatedAt = null
)

fun dummyVisitStockistAllTrue(): Visit = Visit.StockistVisit(
  id = UUID.randomUUID().toString(),
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  employeeId = "f2c2561f-4df8-4894-8256-1f4df8c894f4",
  latitude = 45.367,
  longitude = 43.326,
  distanceMetersFromPOI = 65,
  poiId = "9e54e0bd-0bb7-4a49-94e0-bd0bb72a49cc",
  billNo = "Bill 222345",
  paymentCollected = true,
  samplesGiven = listOf("Capacitor", "Inductor").times(Random.nextInt(1, 3)),
  amountWithGST = Random.nextDouble(300.00, 500.00).toMoney(),
  amountWithoutGST = Random.nextDouble(100.00, 300.00).toMoney(),
  outstandingAmount = "500.00".toMoney(),
  orderTaken = true,
  stockChecked = true,
  additionalNotes = "Stockist is well... a stockist who stocks things permanently, well, kind of...",
  createdAt = Instant.parse("2026-02-11T21:34:04.734+05:30"),
  updatedAt = null
)
