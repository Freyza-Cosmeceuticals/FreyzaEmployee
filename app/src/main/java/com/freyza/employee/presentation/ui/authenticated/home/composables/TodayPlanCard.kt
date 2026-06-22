package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.util.toTitleCase
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlanEntryHoliday
import com.freyza.employee.domain.model.dummyTravelPlanEntryLeave
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun TodayPlanCard(
  planEntry: TravelPlanEntry?,
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
  reportDayType: DayType? = null,
  reportRoute: RouteWithLocation? = null,
) {
  Card(
    elevation = CardDefaults.outlinedCardElevation(),
    colors = CardDefaults.outlinedCardColors(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
      modifier = modifier
        .padding(
          vertical = dimensionResource(R.dimen.default_spacing).times(3),
          horizontal = dimensionResource(R.dimen.default_spacing).times(4)
        )
        .animateContentSize()
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          "Travel Plan".uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary
        )

        if (reportDayType == null && planEntry != null) {
          Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Text(
              "Planned".uppercase(),
              style = MaterialTheme.typography.labelMedium
            )
          }
        } else if (reportDayType != null) {
          Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text(
              "Confirmed".uppercase(),
              style = MaterialTheme.typography.labelMedium
            )
          }
        }
      }

      Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

      if (planEntry == null) {
        Text("No travel plan for today", style = MaterialTheme.typography.bodyMedium)
        return@Column
      }

      val resolvedDayType = reportDayType ?: planEntry.dayType
      val resolvedRoute = reportRoute ?: route

      Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
      ) {
        Text(
          resolvedDayType.name.uppercase(),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
        )
      }

      when (resolvedDayType) {
        DayType.WORK -> {
          WorkStatusContent(resolvedRoute)
        }

        DayType.HOLIDAY -> {
          Text(
            "Enjoy the day!", style = MaterialTheme.typography.bodyMedium
          )
        }

        DayType.LEAVE -> {
          Text(
            "Enjoy your day off!", style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }
  }
}

@Composable
private fun WorkStatusContent(
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
) {
  Box(contentAlignment = Alignment.CenterEnd, modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.CenterVertically
      ), horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()
    ) {
      if (route != null) {
        Text(
          route.srcLoc.name.toTitleCase(),
          style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
          modifier = Modifier.fillMaxWidth()
        )

        Text(
          route.destLoc.name.toTitleCase(),
          style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
          modifier = Modifier.fillMaxWidth()
        )
      } else {
        TravelPlanRouteSkeleton()
        TravelPlanRouteSkeleton()
      }
    }
  }
}

@Composable
private fun TravelPlanRouteSkeleton(modifier: Modifier = Modifier) {
  Skeleton(
    modifier = modifier
      .width(64.dp)
      .height(16.dp)
  )
}

@Composable
fun TravelPlanCardSkeleton(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    elevation = CardDefaults.outlinedCardElevation(),
    colors = CardDefaults.outlinedCardColors(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Text(
      "Travel plan Loading...".uppercase(),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.secondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = dimensionResource(R.dimen.screen_padding))
        .padding(top = dimensionResource(R.dimen.screen_padding))
    )

    TravelPlanRouteSkeleton(
      modifier = Modifier
        .padding(horizontal = dimensionResource(R.dimen.screen_padding))
        .padding(top = 8.dp)
    )

    TravelPlanRouteSkeleton(
      modifier = Modifier
        .padding(vertical = 8.dp, horizontal = dimensionResource(R.dimen.screen_padding))
    )
  }
}

@Preview
@Composable
private fun TodayPlanCardPreviewWork() {
  FreyzaEmployeeTheme {
    TodayPlanCard(
      planEntry = dummyTravelPlanEntryWork(),
      route = dummyRouteWithLocation()
    )
  }
}

@Preview
@Composable
private fun TodayPlanCardPreviewWorkNoRoute() {
  FreyzaEmployeeTheme {
    TodayPlanCard(
      planEntry = dummyTravelPlanEntryWork(),
      route = null
    )
  }
}

@Preview
@Composable
private fun TodayPlanCardPreviewHoliday() {
  FreyzaEmployeeTheme {
    TodayPlanCard(
      planEntry = dummyTravelPlanEntryHoliday(),
      route = null
    )
  }
}

@Preview
@Composable
private fun TodayPlanCardPreviewLeave() {
  FreyzaEmployeeTheme {
    TodayPlanCard(
      planEntry = dummyTravelPlanEntryLeave(),
      route = null
    )
  }
}

@Preview
@Composable
private fun TodayPlanCardNullPreview() {
  FreyzaEmployeeTheme {
    TodayPlanCard(null, null)
  }
}

@Preview
@Composable
private fun TravelPlanCardSkeletonPreview() {
  FreyzaEmployeeTheme {
    TravelPlanCardSkeleton()
  }
}
