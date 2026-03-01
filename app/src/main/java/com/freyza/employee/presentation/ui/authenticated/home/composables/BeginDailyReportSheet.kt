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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.routeName
import com.freyza.employee.presentation.ui.composables.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeginDailyReportSheet(
  dayTypes: List<DayType>,
  routes: UIState<List<RouteWithLocation>>,
  todayTravelPlanEntry: UIState<TravelPlanEntry?>,
  onDailyReportBegin: (dayType: DayType, routeId: String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val planEntry = (todayTravelPlanEntry as? UIState.Ready)?.data

  if (planEntry == null) {
    Box(
      Modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.screen_padding)),
      contentAlignment = Alignment.Center
    ) {
      Text("No travel plan assigned for today")
    }

    return
  }

  var selectedDayType by remember { mutableStateOf(planEntry.dayType) }
  var selectedRoute by remember { mutableStateOf(planEntry.routeId) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredRoutes = remember(searchQuery, routes) {
    derivedStateOf {
      val list = routes.data ?: emptyList()
      if (searchQuery.isBlank()) return@derivedStateOf list

      val parts = searchQuery.split(' ').filter { it.isNotBlank() }
      list.filter { rt ->
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
      .navigationBarsPadding()
      .animateContentSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      dimensionResource(R.dimen.default_spacing), alignment = Alignment.CenterVertically
    )
  ) {
    Text(
      "Begin Daily Report",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(3)))

    SingleChoiceSegmentedButtonRow(
      modifier = Modifier
        .padding(
          horizontal = dimensionResource(R.dimen.default_spacing).times(
            8
          )
        )
        .fillMaxWidth()
    ) {
      dayTypes.forEachIndexed { i, type ->
        SegmentedButton(
          shape = SegmentedButtonDefaults.itemShape(
            index = i, count = com.freyza.employee.domain.model.dayTypes.size
          ), onClick = {
            selectedDayType = type
            if (selectedDayType != DayType.WORK) selectedRoute = null
            else selectedRoute = planEntry.routeId
          }, selected = selectedDayType == type
        ) {
          Text(type.titleCase())
        }
      }
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    AnimatedVisibility(
      visible = selectedDayType == DayType.WORK,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut()
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing)
        )
      ) {
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
            routes.data?.find { it.id == selectedRoute }?.routeName()
              ?: "Select your assigned route",
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
          shape = RoundedCornerShape(integerResource(R.integer.rounding_radius))
        )

        when (routes) {
          is UIState.Ready -> {
            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
              items(filteredRoutes.value, key = { it.id }) { route ->
                val isSelected = selectedRoute == route.id

                ListItem(
                  headlineContent = { Text(route.routeName()) },
                  leadingContent = { Icon(painterResource(R.drawable.route_24px), null) },
                  colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                  supportingContent = { Text("${route.distanceKm} km") },
                  modifier = Modifier
                    .clickable { selectedRoute = route.id }
                    .border(
                      2.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                      RoundedCornerShape(integerResource(R.integer.rounding_radius))
                    ))
              }
            }
          }

          is UIState.Error -> {
            Text("Error loading routes...")
          }

          else -> {
            LoadingIndicator(message = "Loading routes...", modifier = Modifier.fillMaxWidth())
          }
        }
      }
    }
//        else {
//          Text(
//            "No route is needed for non-work days.",
//            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondaryFixedDim)
//          )
//        }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(3)))

    FilledTonalButton(
      modifier = Modifier
        .fillMaxWidth(),
      contentPadding = PaddingValues(dimensionResource(R.dimen.default_spacing).times(4)),
      enabled = (selectedDayType != DayType.WORK || selectedRoute != null),
      onClick = {
        onDailyReportBegin(
          selectedDayType, selectedRoute
        )
      },
    ) { Text("Start Day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
  }
}

@Composable
fun DailyReportingFailedToLoadDialog(
  message: String?,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    properties = DialogProperties(
      dismissOnBackPress = false, dismissOnClickOutside = false
    ),
    onDismissRequest = {},
    title = { Text("Failed to load daily report") },
    text = {
      Text(message ?: "Press Retry to try again")
    },
    confirmButton = {
      TextButton(onClick = onRetry) { Text("Retry") }
    },
    modifier = Modifier,
  )
}
