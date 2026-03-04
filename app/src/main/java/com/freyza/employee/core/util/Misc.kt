package com.freyza.employee.core.util

import android.content.res.Resources.getSystem
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun String.toTitleCase() = this.split(' ').joinToString(" ") { word ->
    word.lowercase().replaceFirstChar { it.titlecase() }
  }

// Source - https://stackoverflow.com/a/62627706
// Posted by nyx69, modified by community. See post 'Timeline' for change history
// Retrieved 2026-01-14, License - CC BY-SA 4.0
val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

fun Dp.toPx(): Int = (this.value * getSystem().displayMetrics.density).fastRoundToInt()

fun LocalDateTime.toLocalDate(): LocalDate = LocalDate(this.year, this.month, this.day)

operator fun <T> Iterable<T>.times(count: Int): List<T> = List(count) { this }.flatten()
