package com.freyza.employee.presentation.ui.authenticated.travelplan.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freyza.employee.core.util.toTitleCase
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.YearMonth

@Composable
fun SimpleCalendarTitle(
  modifier: Modifier,
  currentMonth: YearMonth,
  isHorizontal: Boolean = true,
  goToPrevious: () -> Unit,
  goToNext: () -> Unit,
) {
  Row(
    modifier = modifier.height(40.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
//    CalendarNavigationIcon(
//      painter = painterResource(R.drawable.chevron_left_24px),
//      contentDescription = "Previous",
//      onClick = goToPrevious,
//      isHorizontal = isHorizontal,
//    )
    Text(
      modifier = Modifier
        .weight(1f),
      text = "${currentMonth.month.name.toTitleCase()} ${currentMonth.year}",
      fontSize = 22.sp,
      textAlign = TextAlign.Center,
      fontWeight = FontWeight.Medium,
    )
//    CalendarNavigationIcon(
//      painter = painterResource(R.drawable.chevron_right_24px),
//      contentDescription = "Next",
//      onClick = goToNext,
//      isHorizontal = isHorizontal,
//    )
  }
}

@Composable
private fun CalendarNavigationIcon(
  painter: Painter,
  contentDescription: String,
  isHorizontal: Boolean = true,
  onClick: () -> Unit,
) = Box(
  modifier = Modifier
    .fillMaxHeight()
    .aspectRatio(1f)
    .clip(shape = CircleShape)
    .clickable(role = Role.Button, onClick = onClick),
) {
  val rotation by animateFloatAsState(if (isHorizontal) 0f else 90f)
  Icon(
    painter = painter,
    modifier = Modifier
      .fillMaxSize()
      .padding(4.dp)
      .align(Alignment.Center)
      .rotate(rotation),
    contentDescription = contentDescription,
  )
}

@Composable
fun rememberFirstCompletelyVisibleMonth(state: CalendarState): CalendarMonth {
  val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
  // Only take non-null values as null will be produced when the
  // list is mid-scroll as no index will be completely visible.
  LaunchedEffect(state) {
    snapshotFlow { state.layoutInfo.completelyVisibleMonths.firstOrNull() }
      .filterNotNull()
      .collect { month -> visibleMonth.value = month }
  }
  return visibleMonth.value
}

@Composable
fun rememberFirstVisibleMonthAfterScroll(state: CalendarState): CalendarMonth {
  val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
  LaunchedEffect(state) {
    snapshotFlow { state.isScrollInProgress }
      .filter { scrolling -> !scrolling }
      .collect { visibleMonth.value = state.firstVisibleMonth }
  }
  return visibleMonth.value
}

/**
 * Find the first month on the calendar visible up to the given [viewportPercent] size.
 *
 * @see [rememberFirstCompletelyVisibleMonth]
 * @see [rememberFirstVisibleMonthAfterScroll]
 */
@Composable
fun rememberFirstMostVisibleMonth(
  state: CalendarState,
  viewportPercent: Float = 50f,
): CalendarMonth {
  val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
  LaunchedEffect(state) {
    snapshotFlow { state.layoutInfo.firstMostVisibleMonth(viewportPercent) }
      .filterNotNull()
      .collect { month -> visibleMonth.value = month }
  }
  return visibleMonth.value
}

private val CalendarLayoutInfo.completelyVisibleMonths: List<CalendarMonth>
  get() {
    val visibleItemsInfo = this.visibleMonthsInfo.toMutableList()
    return if (visibleItemsInfo.isEmpty()) {
      emptyList()
    } else {
      val lastItem = visibleItemsInfo.last()
      val viewportSize = this.viewportEndOffset + this.viewportStartOffset
      if (lastItem.offset + lastItem.size > viewportSize) {
        visibleItemsInfo.removeAt(visibleItemsInfo.lastIndex)
      }
      val firstItem = visibleItemsInfo.firstOrNull()
      if (firstItem != null && firstItem.offset < this.viewportStartOffset) {
        visibleItemsInfo.removeAt(0)
      }
      visibleItemsInfo.map { it.month }
    }
  }

private fun CalendarLayoutInfo.firstMostVisibleMonth(viewportPercent: Float = 50f): CalendarMonth? {
  return if (visibleMonthsInfo.isEmpty()) {
    null
  } else {
    val viewportSize = (viewportEndOffset + viewportStartOffset) * viewportPercent / 100f
    visibleMonthsInfo.firstOrNull { itemInfo ->
      if (itemInfo.offset < 0) {
        itemInfo.offset + itemInfo.size >= viewportSize
      } else {
        itemInfo.size - itemInfo.offset >= viewportSize
      }
    }?.month
  }
}


suspend fun LazyListState.animateScrollAndCenterItem(index: Int) {
  suspend fun animateScrollIfVisible(): Boolean {
    val layoutInfo = layoutInfo
    val containerSize =
      layoutInfo.viewportSize.width - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
    val target = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    val targetOffset = containerSize / 2f - target.size / 2f
    animateScrollBy(target.offset - targetOffset)
    return true
  }
  if (!animateScrollIfVisible()) {
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val currentIndex = visibleItemsInfo.getOrNull(visibleItemsInfo.size / 2)?.index ?: -1
    scrollToItem(
      if (index > currentIndex) {
        (index - visibleItemsInfo.size + 1)
      } else {
        index
      }.coerceIn(0, layoutInfo.totalItemsCount),
    )
    animateScrollIfVisible()
  }
}
