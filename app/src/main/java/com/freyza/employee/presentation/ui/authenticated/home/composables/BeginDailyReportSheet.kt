package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dayTypes
import com.freyza.employee.domain.model.dummyLocation
import com.freyza.employee.domain.model.dummyLocationAlt
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlanEntryHoliday
import com.freyza.employee.domain.model.dummyTravelPlanEntryLeave
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.domain.model.dummyUserEmployeeAlt
import com.freyza.employee.presentation.ui.composables.RouteItem
import com.freyza.employee.presentation.ui.composables.SearchableDropdown
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeginDailyReportSheet(
  dayTypes: List<DayType>,
  routes: List<RouteWithLocation>,
  locations: List<Location>,
  employees: List<User>,
  todayTravelPlanEntry: TravelPlanEntry?,
  onDailyReportBegin: (dayType: DayType, srcLocId: String?, destLocId: String?, travellingWithId: String?) -> Unit,
  onRetry: () -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (todayTravelPlanEntry == null) {
    Column(
      modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.screen_padding).times(2)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text("No travel plan assigned for today", style = MaterialTheme.typography.bodyLarge)

      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(3)))

      Row(
        horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(3)
        )
      ) {
        Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
          Text("Refresh")
        }
        OutlinedButton(onClick = onExit, modifier = Modifier.weight(1f)) {
          Text("Exit")
        }
      }
    }

    return
  }


  var selectedDayType by remember(todayTravelPlanEntry) { mutableStateOf(todayTravelPlanEntry.dayType) }

  var selectedSource by remember(todayTravelPlanEntry, routes) {
    mutableStateOf(routes.find { it.id == todayTravelPlanEntry.routeId }?.srcLoc)
  }
  var selectedDestination by remember(todayTravelPlanEntry, routes) {
    mutableStateOf(routes.find { it.id == todayTravelPlanEntry.routeId }?.destLoc)
  }

  var sourceQuery by remember { mutableStateOf(selectedSource?.name ?: "") }
  var destinationQuery by remember { mutableStateOf(selectedDestination?.name ?: "") }

  var selectedTravellingWith by remember { mutableStateOf<User?>(null) }

  val matchingRoute by remember(selectedSource, selectedDestination, routes) {
    derivedStateOf {
      if (selectedSource == null || selectedDestination == null) null
      else routes.find { it.srcLoc.id == selectedSource?.id && it.destLoc.id == selectedDestination?.id }
    }
  }

  val startButtonEnabled by remember(selectedDayType, selectedSource, selectedDestination) {
    derivedStateOf {
      val isWork = selectedDayType == DayType.WORK
      !isWork || (selectedSource != null && selectedDestination != null)
    }
  }

  val startButtonText = remember(selectedDayType) {
    when (selectedDayType) {
      DayType.WORK -> "Start Day"
      DayType.LEAVE -> "Mark Leave"
      DayType.HOLIDAY -> "Mark Holiday"
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(dimensionResource(R.dimen.screen_padding))
      // essential for keyboard
      .imePadding(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      dimensionResource(R.dimen.default_spacing), alignment = Alignment.CenterVertically
    )
  ) {
    Text(
      "Begin Daily Report",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    SingleChoiceSegmentedButtonRow(
      modifier = Modifier
        .padding(horizontal = dimensionResource(R.dimen.default_spacing).times(8))
        .fillMaxWidth()
    ) {
      dayTypes.forEachIndexed { i, type ->
        SegmentedButton(
          shape = SegmentedButtonDefaults.itemShape(
            index = i, count = dayTypes.size
          ), onClick = {
            selectedDayType = type
            if (selectedDayType == DayType.WORK) {
              val route = routes.find { it.id == todayTravelPlanEntry.routeId }
              selectedSource = route?.srcLoc
              selectedDestination = route?.destLoc
              selectedTravellingWith = null
            }
          }, selected = selectedDayType == type
        ) {
          Text(type.titleCase())
        }
      }
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    AnimatedVisibility(
      visible = selectedDayType == DayType.WORK,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically(),
      modifier = Modifier.weight(1f, fill = false)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(
            3
          )
        )
      ) {
        TravellingWithSelector(
          employees = employees,
          selectedEmployee = selectedTravellingWith,
          onEmployeeSelect = { selectedTravellingWith = it }
        )

        SearchableDropdown(
          label = "Source Location",
          items = locations,
          selectedItem = selectedSource,
          onItemSelect = { selectedSource = it },
          query = sourceQuery,
          onQueryChange = { sourceQuery = it },
          itemLabeler = { it.name },
          placeholder = "Search source...",
          leadingIcon = { Icon(painterResource(R.drawable.location_on_24px), null) }
        )

        SearchableDropdown(
          label = "Destination Location",
          items = locations,
          selectedItem = selectedDestination,
          onItemSelect = { selectedDestination = it },
          query = destinationQuery,
          onQueryChange = { destinationQuery = it },
          itemLabeler = { it.name },
          placeholder = "Search destination...",
          leadingIcon = { Icon(painterResource(R.drawable.location_on_24px), null) }
        )

        AnimatedVisibility(visible = matchingRoute != null) {
          ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
              containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              RouteItem(matchingRoute)
            }
          }
        }

        AnimatedVisibility(visible = selectedSource != null && selectedDestination != null && matchingRoute == null) {
          ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
              containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              Text(
                "New Route",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
              )
              RouteItem(
                route = RouteWithLocation(
                  id = "new",
                  srcLoc = selectedSource!!,
                  destLoc = selectedDestination!!,
                  0.0f,
                  Instant.parse("1970-01-01T00:00:00.000+00:00"),
                  updatedAt = null
                )
              )
//              Text(
//                "${selectedSource!!.name} -> ${selectedDestination!!.name}",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//              )
              Text(
                "This route will be created automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(3)))

    Button(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(dimensionResource(R.dimen.default_spacing).times(4)),
      enabled = startButtonEnabled,
      onClick = {
        onDailyReportBegin(
          selectedDayType,
          selectedSource?.id,
          selectedDestination?.id,
          selectedTravellingWith?.id
        )
      },
    ) {
      Text(
        startButtonText,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}


@Preview(showBackground = true)
@Composable
private fun SheetPreviewWork() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      locations = listOf(dummyLocation(), dummyLocationAlt()),
      employees = listOf(dummyUserEmployee(), dummyUserEmployeeAlt()),
      todayTravelPlanEntry = dummyTravelPlanEntryWork(),
      onDailyReportBegin = { _, _, _, _ -> },
      onRetry = {},
      onExit = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SheetPreviewHoliday() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      locations = listOf(dummyLocation(), dummyLocationAlt()),
      employees = listOf(dummyUserEmployeeAlt()),
      todayTravelPlanEntry = dummyTravelPlanEntryHoliday(),
      onDailyReportBegin = { _, _, _, _ -> },
      onRetry = {},
      onExit = {}
    )
  }
}


@Preview(showBackground = true)
@Composable
private fun SheetPreviewLeave() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      locations = listOf(dummyLocation(), dummyLocationAlt()),
      employees = listOf(dummyUserEmployeeAlt()),
      todayTravelPlanEntry = dummyTravelPlanEntryLeave(),
      onDailyReportBegin = { _, _, _, _ -> },
      onRetry = {},
      onExit = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SheetPreviewNoPlan() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      locations = listOf(dummyLocation(), dummyLocationAlt()),
      employees = listOf(dummyUserEmployeeAlt()),
      todayTravelPlanEntry = null,
      onDailyReportBegin = { _, _, _, _ -> },
      onRetry = {},
      onExit = {}
    )
  }
}
