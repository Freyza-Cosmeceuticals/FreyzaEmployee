package com.freyza.employee.domain.model

import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.toMoney
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.time.Instant

data class DailyReport(
  val id: String,
  val employeeId: String,

  val date: LocalDate,
  val dayType: DayType,
  val routeId: String?,

  val ta: Money?,
  val da: Money?,
  val totalExpense: Money?,

  val visits: List<Visit>,

  val locked: Boolean,
  val lockedAt: Instant?,

  val createdAt: Instant,
  val updatedAt: Instant?,
)

fun dummyDailyReportWork(
  locked: Boolean = false,
  dateNow: Boolean = false,
  noVisits: Boolean = false,
): DailyReport = DailyReport(
  id = UUID.randomUUID().toString(),
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = if (dateNow) ServerTime().nowLocalDateTime().date else LocalDate.parse("2026-03-01"),
  dayType = DayType.WORK,
  routeId = "3f6b851f-5c47-42b0-ab85-1f5c47c2b0d7",
  ta = "500.00".toMoney(),
  da = "750.00".toMoney(),
  totalExpense = "1020.00".toMoney(),
  visits = if (noVisits) emptyList() else listOf(
    dummyVisitDoctor(),
    dummyVisitStockist(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitDoctor(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitDoctor(),
    dummyVisitDoctor(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitDoctor(),
    dummyVisitChemist(),
    dummyVisitChemist(),
    dummyVisitDoctor()
  ),
  locked = locked,
  lockedAt = if (locked) Instant.parse("2026-02-11T21:27:33.882+05:30") else null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)

fun dummyDailyReportHoliday(locked: Boolean = false): DailyReport = DailyReport(
  id = UUID.randomUUID().toString(),
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = LocalDate.parse("2026-02-11"),
  dayType = DayType.HOLIDAY,
  routeId = null,
  ta = Money.ZERO,
  da = Money.ZERO,
  totalExpense = Money.ZERO,
  visits = listOf(),
  locked = locked,
  lockedAt = if (locked) Instant.parse("2026-02-11T21:27:33.882+05:30") else null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)

fun dummyDailyReportLeave(locked: Boolean = false): DailyReport = DailyReport(
  id = UUID.randomUUID().toString(),
  employeeId = "25de9fec-f4c0-4927-9e9f-ecf4c0a9271c",
  date = LocalDate.parse("2026-02-11"),
  dayType = DayType.LEAVE,
  routeId = null,
  ta = Money.ZERO,
  da = Money.ZERO,
  totalExpense = Money.ZERO,
  visits = listOf(),
  locked = locked,
  lockedAt = if (locked) Instant.parse("2026-02-11T21:27:33.882+05:30") else null,
  createdAt = Instant.parse("2026-02-11T12:29:21.745+05:30"),
  updatedAt = null
)
