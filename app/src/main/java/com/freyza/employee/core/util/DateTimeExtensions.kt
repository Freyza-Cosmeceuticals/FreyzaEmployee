package com.freyza.employee.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Parses a string to a [LocalDate].
 *
 * It handles both simple date strings (YYYY-MM-DD) and full ISO timestamps.
 */
fun String.toLocalDate(): LocalDate {
  return try {
    if (this.contains('T')) {
      Instant.parse(this).toLocalDateTime(TimeZone.UTC).date
    } else {
      LocalDate.parse(this)
    }
  } catch (_: Exception) {
    // Fallback to basic parse if it fails, which will likely throw the original error
    // if the string is completely invalid.
    LocalDate.parse(this)
  }
}

/**
 * Calculates the number of days left in the month for this [LocalDateTime].
 */
val LocalDateTime.numDaysLeftInMonth: Int
  get() = this.date.numDaysLeftInMonth

/**
 * Calculates the number of days left in the month for this [LocalDate].
 */
val LocalDate.numDaysLeftInMonth: Int
  get() {
    val daysInMonth = when (this.month.number) {
      1, 3, 5, 7, 8, 10, 12 -> 31
      4, 6, 9, 11 -> 30
      2 -> if ((this.year % 4 == 0 && this.year % 100 != 0) || (this.year % 400 == 0)) 29 else 28
      else -> 30
    }
    return maxOf(0, daysInMonth - this.day)
  }
