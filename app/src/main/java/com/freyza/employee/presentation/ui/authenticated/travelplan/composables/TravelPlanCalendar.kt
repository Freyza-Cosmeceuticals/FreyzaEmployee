package com.freyza.employee.presentation.ui.authenticated.travelplan.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyza.employee.core.util.toTitleCase
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaYearMonth
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinYearMonth

@Composable
fun TravelPlanCalendar(
  startMonth: YearMonth,
  endMonth: YearMonth,
  currentMonth: YearMonth,
  daysOfWeek: List<DayOfWeek>,
  selectedDate: CalendarDay?,
  setSelectedDate: (CalendarDay) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = rememberCalendarState(
    startMonth = startMonth.toJavaYearMonth(),
    endMonth = endMonth.toJavaYearMonth(),
    firstVisibleMonth = currentMonth.toJavaYearMonth(),
    firstDayOfWeek = daysOfWeek.first().toJavaDayOfWeek(),
  )

  val coroutineScope = rememberCoroutineScope()
  val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)

  SimpleCalendarTitle(
    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
    currentMonth = visibleMonth.yearMonth.toKotlinYearMonth(),
    goToPrevious = {
      coroutineScope.launch {
        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
      }
    },
    goToNext = {
      coroutineScope.launch {
        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
      }
    },
  )

  HorizontalCalendar(
    state = state,
    dayContent = { day ->
      Day(day, isSelected = selectedDate?.equals(day) ?: false) { clicked ->
        setSelectedDate(clicked)
      }
    },
    monthHeader = {
      MonthHeader(daysOfWeek = daysOfWeek)
    },
  )
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
  Row(
    modifier = Modifier.fillMaxWidth(),
  ) {
    for (dayOfWeek in daysOfWeek) {
      Text(
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        text = dayOfWeek.toString().toTitleCase().substring(0..2),
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
private fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
  Box(
    modifier = Modifier
      .aspectRatio(1f) // This is important for square-sizing!
      .padding(6.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
      // Disable clicks on inDates/outDates
      .clickable(
        enabled = day.position == DayPosition.MonthDate,
        onClick = { onClick(day) },
      ),
    contentAlignment = Alignment.Center,
  ) {
    val textColor = when (day.position) {
      // Color.Unspecified will use the default text color from the current theme
      DayPosition.MonthDate -> if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
      DayPosition.InDate, DayPosition.OutDate -> MaterialTheme.colorScheme.secondaryFixedDim.copy(
        alpha = 0.4f
      )
    }
    Text(
      text = day.date.toKotlinLocalDate().day.toString(),
      color = textColor,
      fontSize = 14.sp,
    )
  }
}
