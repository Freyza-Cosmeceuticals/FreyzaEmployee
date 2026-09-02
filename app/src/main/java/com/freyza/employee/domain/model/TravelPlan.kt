package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class TravelPlan(
  val id: String,
  val employeeId: String,
  val month: LocalDate,
  val createdById: String,

  val travelPlanEntries: List<TravelPlanEntry>,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

data class TravelPlanMetrics(
  val targetAmount: Double,
  val employeeId: String,
  val totalOrderAmount: Double,
  val totalAmountWithoutGST: Double,
  val numReports: Int,
  val numVisits: Int,
) {
  val currentAmount: Double
    get() = totalOrderAmount + totalAmountWithoutGST

  val percentage: Float
    get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()) * 100f else 0f
}

data class TravelPlanEntry(
  val id: String,
  val tpId: String,

  val date: LocalDate,
  val dayType: DayType,
  val routeId: String?,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

enum class DayType {
  WORK, HOLIDAY, LEAVE;

  fun titleCase(): String =
    this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)

}

val dayTypes = listOf(DayType.WORK, DayType.HOLIDAY, DayType.LEAVE)

fun dummyTravelPlan(): TravelPlan = TravelPlan(
  id = "2d201f46-0310-4a9a-a01f-4603108a9af5",
  employeeId = "a79ae89b-af0f-4f0f-9ae8-9baf0f4f0f59",
  month = LocalDate.parse("2026-01-01"),
  createdById = "857e6915-f617-4e5f-be69-15f6175e5f9a",
  travelPlanEntries = listOf(
    TravelPlanEntry(
      id = "6f813189-94e1-49a6-8131-8994e169a656",
      tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
      date = LocalDate.parse("2026-01-01"),
      dayType = DayType.WORK,
      routeId = "69c07d79-d679-48f6-807d-79d67948f675",
      createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
      updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
    ), TravelPlanEntry(
      id = "8dc07977-bb02-4a9e-8079-77bb02ba9ebe",
      tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
      date = LocalDate.parse("2026-01-02"),
      dayType = DayType.LEAVE,
      routeId = null,
      createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
      updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
    ), TravelPlanEntry(
      id = "1ce70926-e09a-457e-a709-26e09a257e39",
      tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
      date = LocalDate.parse("2026-01-03"),
      dayType = DayType.HOLIDAY,
      routeId = null,
      createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
      updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
    )
  ),
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)

fun dummyTravelPlanMetrics(): TravelPlanMetrics = TravelPlanMetrics(
  targetAmount = 15000.0,
  employeeId = "a79ae89b-af0f-4f0f-9ae8-9baf0f4f0f59",
  totalOrderAmount = 8000.0,
  totalAmountWithoutGST = 4500.0,
  numReports = 5,
  numVisits = 34
)

fun dummyTravelPlanEntryWork() = TravelPlanEntry(
  id = "680631fd-43d8-41c9-8631-fd43d821c9ca",
  tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
  date = LocalDate.parse("2026-01-02"),
  dayType = DayType.WORK,
  routeId = "99fe0183-502d-4b2b-be01-83502ddb2b74",
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)

fun dummyTravelPlanEntryHoliday() = TravelPlanEntry(
  id = "ee936123-4e68-4c01-9361-234e683c01e0",
  tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
  date = LocalDate.parse("2026-01-03"),
  dayType = DayType.HOLIDAY,
  routeId = null,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)


fun dummyTravelPlanEntryLeave() = TravelPlanEntry(
  id = "8dc07977-bb02-4a9e-8079-77bb02ba9ebe",
  tpId = "2d201f46-0310-4a9a-a01f-4603108a9af5",
  date = LocalDate.parse("2026-01-04"),
  dayType = DayType.LEAVE,
  routeId = null,
  createdAt = Instant.parse("2026-01-10T08:05:02.681+00:00"),
  updatedAt = Instant.parse("2026-01-10T08:05:02.681+00:00")
)
