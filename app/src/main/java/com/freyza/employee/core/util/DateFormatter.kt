package com.freyza.employee.core.util

import com.freyza.employee.core.Constants
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateFormatter {
  enum class FormattingType {
    MACHINE,
    HUMAN,
  }

  val dateFormatter = LocalDate.Format {
    year(); char('-'); monthNumber(); char('-'); day()
  }

  val humanDateFormatter = LocalDate.Format {
    day(Padding.NONE); char(' '); monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); year()
  }

  val humanShortDateFormatter = LocalDate.Format {
    day(Padding.ZERO); char(' '); monthName(MonthNames.ENGLISH_FULL)
  }

  val dateTimeFormatter = LocalDateTime.Format {
    year(); char('-'); monthNumber(); char('-'); day()
    char('T')
    hour(); char(':'); minute(); char(':'); second(); char('.'); secondFraction(3)
  }

  val humanDateTimeFormatter = LocalDateTime.Format {
    day(Padding.NONE); char(' '); monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); year()
    char(' ')
    amPmHour(); char(':'); minute(); amPmMarker("AM", "PM")
  }

  /**
   * `short: Boolean = false` Keep formatted date short (omit year)
   */
  fun format(
    it: LocalDate,
    formattingType: FormattingType = FormattingType.HUMAN,
    short: Boolean = false,
  ): String {
    return when (formattingType) {
      FormattingType.MACHINE -> dateFormatter.format(it)
      FormattingType.HUMAN -> {
        if (short) humanShortDateFormatter.format(it)
        else humanDateFormatter.format(it)
      }
    }
  }

  fun format(it: LocalDateTime, formattingType: FormattingType = FormattingType.HUMAN): String {
    return when (formattingType) {
      FormattingType.MACHINE -> dateTimeFormatter.format(it)
      FormattingType.HUMAN -> humanDateTimeFormatter.format(it)
    }
  }

  fun format(it: Instant, formattingType: FormattingType = FormattingType.HUMAN): String {
    return when (formattingType) {
      FormattingType.MACHINE -> dateTimeFormatter.format(
        it.toLocalDateTime(
          TimeZone.of(
            Constants.TIMEZONE
          )
        )
      )

      FormattingType.HUMAN -> humanDateTimeFormatter.format(
        it.toLocalDateTime(
          TimeZone.of(
            Constants.TIMEZONE
          )
        )
      )
    }
  }
}
