package com.freyza.employee.presentation.ui.authenticated.travelplan.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.toTitleCase
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.presentation.ui.authenticated.home.composables.TodayPlanCard
import com.kizitonwose.calendar.core.CalendarDay
import kotlinx.datetime.toKotlinLocalDate

@Composable
fun DayBottomSheetContent(
  selectedDate: CalendarDay,
  selectedPlanEntry: TravelPlanEntry?,
  selectedRoute: UIState<Route?>,
  selectedSrcDestPair: UIState<Pair<Location, Location>?>,
  onClickPrevious: () -> Unit,
  onClickNext: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.padding(
      vertical = dimensionResource(R.dimen.default_spacing).times(2),
      horizontal = dimensionResource(R.dimen.default_spacing).times(4)
    )
  ) {
    Row(
      modifier = modifier
        .height(48.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(1f)
          .clip(CircleShape)
          .clickable(role = Role.Button, onClick = onClickPrevious)
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_left_24px),
          modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center),
          contentDescription = "Previous",
        )
      }

      Spacer(Modifier.weight(1f))
      Text("${selectedDate.date.toKotlinLocalDate().day} ${selectedDate.date.toKotlinLocalDate().month.name.toTitleCase()}")
      Spacer(Modifier.weight(1f))

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(1f)
          .clip(CircleShape)
          .clickable(role = Role.Button, onClick = onClickNext)
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_right_24px),
          modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center),
          contentDescription = "Next",
        )
      }
    }

    if (selectedPlanEntry != null) {
      TodayPlanCard(
        selectedPlanEntry, selectedRoute, selectedSrcDestPair
      )
    } else {
      Text("No Travel Plan Entry this day")
    }
  }
}
