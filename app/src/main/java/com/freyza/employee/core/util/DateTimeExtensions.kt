package com.freyza.employee.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
