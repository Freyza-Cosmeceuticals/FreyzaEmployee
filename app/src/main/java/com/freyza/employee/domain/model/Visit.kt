package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.freyza.employee.R
import com.freyza.employee.core.util.times
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Instant

sealed class Visit {
  abstract val id: String
  abstract val reportId: String
  abstract val employeeId: String
  abstract val visitType: VisitType

  abstract val latitude: Double
  abstract val longitude: Double
  abstract val distanceMetersFromPOI: Int

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

    val doctorName: String,
    val productsShown: List<String> = emptyList(),
    val samplesGiven: List<String> = emptyList(),
    val orderTaken: Boolean = false,

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

    val stockistName: String,
    val productsShown: List<String> = emptyList(),
    val samplesGiven: List<String> = emptyList(),
    val orderTaken: Boolean = false,
    val billNo: String,
    val paymentCollected: Boolean,
    val amountWithGST: Double,
    val amountWithoutGST: Double,
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

    val chemistName: String,
    val productsShown: List<String> = emptyList(),
    val orderTaken: Boolean = false,

    override val additionalNotes: String?,

    override val createdAt: Instant,
    override val updatedAt: Instant?,
  ) : Visit() {
    override val visitType: VisitType = VisitType.CHEMIST
  }
}

data class VisitCreate(
  val doctorName: String?,
  val stockistName: String?,
  val chemistName: String?,
  val productsShown: List<String>,
  val samplesGiven: List<String>,
  val orderTaken: Boolean,
  val billNo: String?,
  val paymentCollected: Boolean,
  val amountWithGST: Double,
  val amountWithoutGST: Double,
  val stockChecked: Boolean,
  val notes: String?,
)

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
  doctorName = "Dr. X",
  productsShown = if (Random.nextBoolean()) listOf(
    "Generator", "Repulsor", "Reactor"
  ).times(Random.nextInt(1, 3)) else emptyList(),
  samplesGiven = if (Random.nextBoolean()) listOf("Capacitor", "Inductor").times(
    Random.nextInt(
      1, 3
    )
  ) else emptyList(),
  orderTaken = Random.nextBoolean(),
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
  doctorName = "Dr. X",
  productsShown = listOf("Generator", "Repulsor", "Reactor").times(Random.nextInt(1, 3)),
  samplesGiven = listOf("Capacitor", "Inductor").times(Random.nextInt(1, 3)),
  orderTaken = true,
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
  chemistName = "Ch. Y",
  productsShown = if (Random.nextBoolean()) listOf("Mineral", "Liquid").times(
    Random.nextInt(
      1, 3
    )
  ) else emptyList(),
  orderTaken = Random.nextBoolean(),
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
  chemistName = "Ch. Y",
  productsShown = listOf("Mineral", "Liquid").times(Random.nextInt(1, 3)),
  orderTaken = true,
  additionalNotes = "Chemist was good",
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
  stockistName = "Stockist XYZ Holmes",
  billNo = "Bill 222345",
  paymentCollected = Random.nextBoolean(),
  amountWithGST = Random.nextDouble(300.00, 500.00),
  amountWithoutGST = Random.nextDouble(100.00, 300.00),
  stockChecked = Random.nextBoolean(),
  productsShown = if (Random.nextBoolean()) listOf(
    "Generator", "Repulsor", "Reactor"
  ).times(Random.nextInt(1, 3)) else emptyList(),
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
  stockistName = "Stockist XYZ Holmes",
  billNo = "Bill 222345",
  paymentCollected = true,
  productsShown = listOf("Generator", "Repulsor", "Reactor").times(Random.nextInt(1, 3)),
  samplesGiven = listOf("Capacitor", "Inductor").times(Random.nextInt(1, 3)),
  amountWithGST = Random.nextDouble(300.00, 500.00),
  amountWithoutGST = Random.nextDouble(100.00, 300.00),
  orderTaken = true,
  stockChecked = true,
  additionalNotes = "Stockist is well... a stockist who stocks things permanently, well, kind of...",
  createdAt = Instant.parse("2026-02-11T21:34:04.734+05:30"),
  updatedAt = null
)
