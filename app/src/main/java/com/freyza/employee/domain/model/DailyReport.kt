package com.freyza.employee.domain.model

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class DailyReport(
  val id: String,
  val employeeId: String,

  val date: LocalDate,
  val dayType: DayType,
  val routeId: String?,

  val ta: Double?,
  val da: Double?,
  val totalExpense: Double?,

  val visits: List<Visit>,

  val locked: Boolean,
  val lockedAt: Instant?,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

data class Visit(
  val id: String,
  val reportId: String,

  val visitType: VisitType,

  val latitude: Double,
  val longitude: Double,

  val distanceMetersFromPOI: Int,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

enum class VisitType {
  DOCTOR, STOCKIST, CHEMIST;

  fun titleCase(): String =
    this.name[0].titlecase() + this.name.substring(1).toLowerCase(Locale.current)
}

fun dummyDailyReportWork(locked: Boolean = false): DailyReport = DailyReport(
  id = "4eed577c-8848-41b4-ad57-7c8848c1b499",
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = LocalDate.parse("2026-02-11"),
  dayType = DayType.WORK,
  routeId = "e2556d86-ccef-4bab-956d-86ccefcbabe4",
  ta = 500.00,
  da = 750.00,
  totalExpense = 1020.00,
  visits = listOf(
    Visit(
      id = "cef06593-941f-4401-b065-93941ff4018c",
      reportId = "4eed577c-8848-41b4-ad57-7c8848c1b499",
      visitType = VisitType.DOCTOR,
      latitude = 34.890,
      longitude = 78.216,
      distanceMetersFromPOI = 53,
      createdAt = Instant.parse("2026-02-11T21:30:39.791+05:30"),
      updatedAt = null
    ), Visit(
      id = "b8ba5665-7dc5-41b8-ba56-657dc511b804",
      reportId = "4eed577c-8848-41b4-ad57-7c8848c1b499",
      visitType = VisitType.CHEMIST,
      latitude = 54.790,
      longitude = 43.236,
      distanceMetersFromPOI = 21,
      createdAt = Instant.parse("2026-02-11T21:31:18.290+05:30"),
      updatedAt = null
    )
  ),
  locked = locked,
  lockedAt = if (locked) Instant.parse("2026-02-11T21:27:33.882+05:30") else null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)

fun dummyDailyReportHoliday(): DailyReport = DailyReport(
  id = "4eed577c-8848-41b4-ad57-7c8848c1b499",
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = LocalDate.parse("2026-02-11"),
  dayType = DayType.HOLIDAY,
  routeId = null,
  ta = 0.00,
  da = 0.00,
  totalExpense = 0.00,
  visits = listOf(),
  locked = false,
  lockedAt = null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)

fun dummyDailyReportLeave(): DailyReport = DailyReport(
  id = "4eed577c-8848-41b4-ad57-7c8848c1b499",
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = LocalDate.parse("2026-02-11"),
  dayType = DayType.LEAVE,
  routeId = null,
  ta = 0.00,
  da = 0.00,
  totalExpense = 0.00,
  visits = listOf(),
  locked = false,
  lockedAt = null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)

fun dummyVisitDoctor(): Visit = Visit(
  id = "68829f32-52d5-419f-829f-3252d5819f79",
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  visitType = VisitType.DOCTOR,
  latitude = 34.632,
  longitude = 55.246,
  distanceMetersFromPOI = 55,
  createdAt = Instant.parse("2026-02-11T21:32:38.409+05:30"),
  updatedAt = null
)

fun dummyVisitChemist(): Visit = Visit(
  id = "0703bdf8-2fe9-43ee-83bd-f82fe9b3eede",
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  visitType = VisitType.CHEMIST,
  latitude = 45.653,
  longitude = 22.216,
  distanceMetersFromPOI = 34,
  createdAt = Instant.parse("2026-02-11T21:33:28.453+05:30"),
  updatedAt = null
)

fun dummyVisitStockist(): Visit = Visit(
  id = "cade37a7-f57d-48a6-9e37-a7f57dc8a658",
  reportId = "60dd615b-367a-4691-9d61-5b367af691ba",
  visitType = VisitType.STOCKIST,
  latitude = 45.367,
  longitude = 43.326,
  distanceMetersFromPOI = 65,
  createdAt = Instant.parse("2026-02-11T21:34:04.734+05:30"),
  updatedAt = null
)
