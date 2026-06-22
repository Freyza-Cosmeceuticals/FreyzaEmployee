package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dayTypes
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlanEntryHoliday
import com.freyza.employee.domain.model.dummyTravelPlanEntryLeave
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import com.freyza.employee.domain.model.routeName
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeginDailyReportSheet(
  dayTypes: List<DayType>,
  routes: List<RouteWithLocation>,
  todayTravelPlanEntry: TravelPlanEntry?,
  onDailyReportBegin: (dayType: DayType, routeId: String?) -> Unit,
  onRetry: () -> Unit,
  onExit: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier,
  isLoadingRoutes: Boolean = false,
) {

  if (todayTravelPlanEntry == null) {
    Column(
      Modifier
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
        Button(onClick = onExit, modifier = Modifier.weight(1f)) {
          Text("Exit")
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f)) {
          Text("Logout")
        }
      }
    }

    return
  }


  var selectedDayType by remember(todayTravelPlanEntry) { mutableStateOf(todayTravelPlanEntry.dayType) }
  var selectedRoute by remember(todayTravelPlanEntry) { mutableStateOf(todayTravelPlanEntry.routeId) }
  var searchQuery by remember { mutableStateOf("") }

  val focusManager = LocalFocusManager.current

  val selectedRouteName by remember(routes, selectedRoute) {
    derivedStateOf {
      val route = routes.find { it.id == selectedRoute }
      route?.routeName() ?: "Select your assigned route"
    }
  }

  val startButtonEnabled by remember(routes, selectedDayType, selectedRoute) {
    derivedStateOf {
      val isWork = selectedDayType == DayType.WORK
      val isRouteSelected = selectedRoute != null

      !isWork || isRouteSelected
    }
  }
  val startButtonText = remember(selectedDayType) {
    when (selectedDayType) {
      DayType.WORK -> "Start Day"
      DayType.LEAVE -> "Mark Leave"
      DayType.HOLIDAY -> "Mark Holiday"
    }
  }

  val filteredRoutes = remember(searchQuery, routes) {
    derivedStateOf {
      if (searchQuery.isBlank()) return@derivedStateOf routes

      val parts = searchQuery.split(' ').filter { it.isNotBlank() }
      routes.filter { rt ->
        when {
          parts.isEmpty() -> true
          parts.size == 1 -> rt.routeName().contains(parts[0], ignoreCase = true)
          else -> rt.srcLoc.name.contains(parts[0], ignoreCase = true) && rt.destLoc.name.contains(
            parts[1], ignoreCase = true
          )
        }
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(dimensionResource(R.dimen.screen_padding))
      .imePadding()
      .animateContentSize(),
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
            selectedRoute = if (selectedDayType != DayType.WORK) null
            else todayTravelPlanEntry.routeId
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
      Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing))) {
        ElevatedCard(
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
              if (selectedRoute != null) 32.dp else 0.dp
            )
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        ) {
          Text(
            selectedRouteName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
          )
        }

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.screen_padding)),
          placeholder = { Text("Search routes...") },
          leadingIcon = { Icon(painterResource(R.drawable.search_24px), null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(painterResource(R.drawable.backspace_24px), "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp)
        )

        if (isLoadingRoutes) {
          LoadingIndicator(message = "Loading routes...", modifier = Modifier.fillMaxWidth())
        } else {
          LazyColumn {
            items(filteredRoutes.value, key = { it.id }) { route ->
              val isSelected = selectedRoute == route.id

              ListItem(
                headlineContent = { Text(route.routeName()) },
                leadingContent = { Icon(painterResource(R.drawable.route_24px), "Route") },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                supportingContent = { Text("${route.distanceKm} km") },
                modifier = Modifier
                  .clickable {
                    selectedRoute = route.id
                    focusManager.clearFocus()
                  }
                  .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                    RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp)
                  ))
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
          selectedDayType, selectedRoute
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
      todayTravelPlanEntry = dummyTravelPlanEntryWork(),
      onDailyReportBegin = { _, _ -> },
      onRetry = {},
      onExit = {},
      onLogout = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun SheetPreviewHoliday() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      todayTravelPlanEntry = dummyTravelPlanEntryHoliday(),
      onDailyReportBegin = { _, _ -> },
      onRetry = {},
      onExit = {},
      onLogout = {})
  }
}


@Preview(showBackground = true)
@Composable
private fun SheetPreviewLeave() {
  FreyzaEmployeeTheme {
    BeginDailyReportSheet(
      dayTypes = dayTypes,
      routes = listOf(dummyRouteWithLocation()),
      todayTravelPlanEntry = dummyTravelPlanEntryLeave(),
      onDailyReportBegin = { _, _ -> },
      onRetry = {},
      onExit = {},
      onLogout = {})
  }
}
