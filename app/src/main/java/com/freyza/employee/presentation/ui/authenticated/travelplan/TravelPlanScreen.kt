package com.freyza.employee.presentation.ui.authenticated.travelplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.authenticated.travelplan.composables.DayBottomSheetContent
import com.freyza.employee.presentation.ui.authenticated.travelplan.composables.TravelPlanCalendar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.FreyzaTravelPlanAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.LocalSnackbarHostState
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.TravelPlanUiState
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinDayOfWeek
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock

@Composable
fun TravelPlanScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: TravelPlanViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
) {
  AuthenticatedRouteWrapper(
    mainUiState, onNavigateToUnauthenticated,
    loading = {
      Logger.e(
        "TravelPlanScreenRoute", "Invalid User/Session on travelplan screen, waiting for 5seconds"
      )
      Skeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
    },
    timeoutMillis = 5_000,
  ) { mainUiState ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TravelPlanScreen(
      uiState,
      mainUiState,
      modifier,
      loadSelectedPlanEntryRoute = viewModel::loadSelectedPlanEntryRoute
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanScreen(
  uiState: TravelPlanUiState,
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  loadSelectedPlanEntryRoute: (tpEntryId: String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState()

  val currentMonth = remember { Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE)).yearMonth }
  val startMonth = remember { currentMonth }
  val endMonth = remember { currentMonth }
  val daysOfWeek = remember { daysOfWeek().map { it.toKotlinDayOfWeek() } }

  var selectedDate by remember { mutableStateOf<CalendarDay?>(null) }
  val selectedPlanEntry by remember(selectedDate) {
    derivedStateOf {
      uiState.travelPlanEntries.data?.find {
        it.date == selectedDate!!.date.toKotlinLocalDate()
      }
    }
  }

  Scaffold(
    topBar = { FreyzaTravelPlanAppBar() },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) { paddingValues ->
    if (mainUiState.user == null || mainUiState.today == null) {
      return@Scaffold
    }

    selectedDate?.let {
      when (uiState.travelPlanEntries) {
        is UIState.Ready -> {
          ModalBottomSheet(
            onDismissRequest = {
              selectedDate = null
            },
            sheetState = sheetState,
          ) {
            Box {
              DayBottomSheetContent(
                selectedDate!!,
                selectedPlanEntry = selectedPlanEntry,
                selectedRoute = uiState.selectedRoute,
                onClickPrevious = {
                  val previous = selectedDate!!.date.toKotlinLocalDate().minus(1, DateTimeUnit.DAY)
                  if (previous.month == selectedDate!!.date.toKotlinLocalDate().month) {
                    selectedDate = CalendarDay(previous.toJavaLocalDate(), selectedDate!!.position)
                  }

                  if (selectedPlanEntry != null && selectedPlanEntry!!.dayType == DayType.WORK) {
                    loadSelectedPlanEntryRoute(selectedPlanEntry!!.id)
                  }
                },
                onClickNext = {
                  val next = selectedDate!!.date.toKotlinLocalDate().plus(1, DateTimeUnit.DAY)
                  if (next.month == selectedDate!!.date.toKotlinLocalDate().month) {
                    selectedDate = CalendarDay(next.toJavaLocalDate(), selectedDate!!.position)
                  }

                  if (selectedPlanEntry != null && selectedPlanEntry!!.dayType == DayType.WORK) {
                    loadSelectedPlanEntryRoute(selectedPlanEntry!!.id)
                  }
                }
              )

              FreyzaSnackbarHost(
                hostState = LocalSnackbarHostState.current, modifier = Modifier
                  .padding(bottom = 16.dp)
              )
            }
          }
        }

        else -> {
          ModalBottomSheet(
            onDismissRequest = {
              selectedDate = null
            },
            sheetState = sheetState,
          ) {
            LoadingIndicator(
              modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
              message = "Loading Day Details..."
            )
          }
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(
        vertical = dimensionResource(R.dimen.default_spacing).times(8),
        horizontal = dimensionResource(R.dimen.default_spacing).times(4)
      ),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {

      item {
        when (val travelPlan = uiState.currentTravelPlan) {
          is UIState.Loading -> {
            LoadingIndicator(message = "Loading Travel Plan..")
          }

          is UIState.Ready -> {
//            DebugTravelPlan(travelPlan.data)
          }

          is UIState.Error -> {
            Text(travelPlan.message.toString())
          }

          else -> {}
        }
      }

      item {
        when (val travelPlan = uiState.currentTravelPlan) {
          is UIState.Ready -> {
            if (travelPlan.data != null) {
              TravelPlanCalendar(
                startMonth = startMonth,
                endMonth = endMonth,
                currentMonth = currentMonth,
                daysOfWeek = daysOfWeek,
                selectedDate = selectedDate,
                setSelectedDate = {
                  selectedDate = it
                  if (selectedPlanEntry != null && selectedPlanEntry!!.dayType == DayType.WORK) {
                    loadSelectedPlanEntryRoute(selectedPlanEntry!!.id)
                  }
                })
            }
          }

          else -> {}
        }
      }
    }
  }
}

@Composable
private fun DebugTravelPlan(travelPlan: TravelPlan?, modifier: Modifier = Modifier) {
  Card {
    Column(modifier = modifier.padding(8.dp)) {

      if (travelPlan === null) {
        Text("No Travel Plan")
      } else {
        Text(travelPlan.id, fontFamily = FontFamily.Monospace)
        Text(travelPlan.employeeId)
        Text(DateFormatter.format(travelPlan.month))
        Text(travelPlan.createdById)

        Text(travelPlan.travelPlanEntries.size.toString())

        Text(DateFormatter.format(travelPlan.createdAt))
        travelPlan.updatedAt?.let {
          Text(DateFormatter.format(it))
        }
      }
    }
  }
}
