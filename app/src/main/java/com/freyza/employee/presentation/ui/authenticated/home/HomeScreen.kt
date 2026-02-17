package com.freyza.employee.presentation.ui.authenticated.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.timedGreeting
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dayTypes
import com.freyza.employee.domain.model.dummyExpenses
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyTravelPlan
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.authenticated.home.composables.BeginDailyReportSheet
import com.freyza.employee.presentation.ui.authenticated.home.composables.DailyReportingFailedToLoadDialog
import com.freyza.employee.presentation.ui.authenticated.home.composables.HomeScreenSkeleton
import com.freyza.employee.presentation.ui.authenticated.home.composables.TodayPlanCard
import com.freyza.employee.presentation.ui.authenticated.home.composables.TravelPlanCardSkeleton
import com.freyza.employee.presentation.ui.composables.FreyzaFabButton
import com.freyza.employee.presentation.ui.composables.FreyzaHomeAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
) {
  AuthenticatedRouteWrapper(
    mainUiState, onNavigateToUnauthenticated,
    loading = {
      Logger.e("HomeScreenRoute", "Invalid User/Session on home screen, waiting for 5 seconds")
      HomeScreenSkeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
    },
    5_000,
  ) { mainUiState ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
      uiState,
      mainUiState,
      modifier,
      onRefresh = viewModel::refresh,
      onDailyReportBegin = viewModel::createCurrentDailyReport,
      onDailyReportRetry = viewModel::loadCurrentDailyReport
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
  uiState: HomeScreenUiState,
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  onRefresh: () -> Unit,
  onDailyReportBegin: (dayType: DayType, routeId: String?) -> Unit,
  onDailyReportRetry: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

  val reportCreationSheetState = rememberModalBottomSheetState(
    confirmValueChange = { newValue -> newValue != SheetValue.Hidden },
    skipPartiallyExpanded = true
  )
  val pullToRefreshState = rememberPullToRefreshState()

  Scaffold(
    topBar = { FreyzaHomeAppBar(today = mainUiState.today, scrollBehavior = scrollBehavior) },
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    ),
    floatingActionButton = { FreyzaFabButton() },
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
  ) {
    if (mainUiState.user == null || mainUiState.today == null) {
      return@Scaffold
    }

    // daily report creation dialog
    // TODO: Note to self, this thing is triggered only on the Home Screen
    // if the user switches tab/page before the bottom sheet shows up, they can perform other app actions
    // but this will eventually show up when they go back to the home screen
    when (val result = uiState.currentDailyReport) {
      is UIState.Ready -> {
        if (result.data == null) {
          // no daily report, ask the user to create one in a blocking way.
          ModalBottomSheet(
            onDismissRequest = {},
            sheetState = reportCreationSheetState,
            sheetGesturesEnabled = false,
            scrimColor = Color.Black.copy(alpha = 0.75f),
            properties = ModalBottomSheetProperties(
              shouldDismissOnBackPress = false,
              shouldDismissOnClickOutside = false
            )
          ) {
            BeginDailyReportSheet(
              dayTypes = dayTypes,
              routes = uiState.routes,
              todayTravelPlanEntry = uiState.todayTravelPlanEntry,
              onDailyReportBegin = onDailyReportBegin
            )
          }
        }
      }

      is UIState.Loading -> {
        ModalBottomSheet(
          onDismissRequest = {},
          sheetState = reportCreationSheetState,
          sheetGesturesEnabled = true,
          properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false,
            shouldDismissOnClickOutside = false
          )
        ) {
          LoadingIndicator(
            Modifier
              .fillMaxWidth()
              .padding(dimensionResource(R.dimen.screen_padding).times(2)),
            result.message ?: "Working on it..."
          )
        }
      }

      is UIState.Error -> {
        // since loading daily report data failed, we cannot continue and ask the user to retry
        DailyReportingFailedToLoadDialog(message = result.message, onRetry = onDailyReportRetry)
      }

      else -> {}
    }

    PullToRefreshBox(
      isRefreshing = uiState.todayPlanEntryRoute is UIState.Loading || uiState.currentDailyReport is UIState.Loading,
      onRefresh = onRefresh,
      state = pullToRefreshState
    ) {
      LazyColumn(
        contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
          .fillMaxSize()
          .padding(it)
          .padding(horizontal = dimensionResource(R.dimen.screen_padding))
      ) {
        item {
          Text(
            mainUiState.today.timedGreeting(mainUiState.user.name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.default_spacing).times(2))
          )
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Min)
              .heightIn(min = 108.dp),
            horizontalArrangement = Arrangement.spacedBy(
              dimensionResource(R.dimen.default_spacing).times(4)
            ),
            verticalAlignment = Alignment.CenterVertically
          ) {
//          TodayCard(
//            mainUiState.today, modifier = Modifier
//              .weight(1f)
//              .fillMaxSize()
//          )

            when (uiState.todayTravelPlanEntry) {
              is UIState.Ready -> {
                TodayPlanCard(
                  planEntry = uiState.todayTravelPlanEntry.data,
                  route = uiState.todayPlanEntryRoute,
                  reportDayType = uiState.todayReportDayType,
                  reportRoute = uiState.todayReportRoute,
                  isPending = uiState.todayReportDayType is UIState.Loading || uiState.todayReportRoute is UIState.Loading,
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                )
              }

              is UIState.Error -> {
                Text(
                  "Error Fetching Travel Plan Entry",
                  style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                )
              }

              else -> {
                TravelPlanCardSkeleton(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                )
              }
            }
          }

          Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing)))
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Min)
              .heightIn(min = 128.dp),
            horizontalArrangement = Arrangement.spacedBy(
              16.dp, Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Card(
              modifier = Modifier
                .weight(1f)
                .fillMaxSize()
            ) {
              Text("Some Graphs Here", modifier = Modifier.padding(16.dp))
            }
          }
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Min)
              .heightIn(min = 128.dp),
            horizontalArrangement = Arrangement.spacedBy(
              16.dp, Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Card(
              modifier = Modifier
                .weight(1f)
                .fillMaxSize()
            ) {
              Text("Here as well", modifier = Modifier.padding(16.dp))
            }
          }
        }

        item {
          Text(
            "Today's Report",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.default_spacing).times(2))
              .padding(top = dimensionResource(R.dimen.default_spacing))
          )
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Min)
              .heightIn(min = 164.dp),
            horizontalArrangement = Arrangement.spacedBy(
              dimensionResource(R.dimen.default_spacing).times(4), Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Card(
              modifier = Modifier
                .weight(1f)
                .fillMaxSize()
            ) {
              Text(
                "Daily Report Data",
                modifier = Modifier.padding(dimensionResource(R.dimen.default_spacing).times(4))
              )
            }
          }
        }

        item {
          DebugUserInfo(mainUiState.user)
        }

        // DebugUserInfo(mainUiState.user)
        // ExpenseList(uiState.data!!.recentExpenses)
      }
    }
  }
}


@Composable
private fun DebugUserInfo(user: User, modifier: Modifier = Modifier) {
  Card {
    Column(modifier = modifier.padding(8.dp)) {

      Text(user.id, fontFamily = FontFamily.Monospace)
      Text(user.name)
      Text(user.email)
      Text(user.phone)

      Text(user.role.titleCase())
      Text(user.status.titleCase())

      Text(user.tier?.fullForm.toString())
      Text(user.hqId.toString())

      Text(DateFormatter.format(user.createdAt))
      user.updatedAt?.let {
        Text(DateFormatter.format(it))
      }

      user.userInfo?.lastSignInAt?.let {
        Text(DateFormatter.format(it))
      }
    }
  }
}

@Composable
fun ExpenseList(expenses: List<Expense>) {

  if (expenses.isEmpty()) {
    Text("No Recent Expenses Found")
  }

  LazyColumn {
    items(expenses) {
      Card(modifier = Modifier.padding(8.dp)) {
        Text(it.id)
        Text(it.location)
        Text("${it.distance}km")
        Text("$${it.cost}")
        Text(if (it.locked) "Locked" else "Open")
      }
    }
  }
}

@Preview(
  showSystemUi = true, showBackground = true
)
@Composable
private fun HomeScreenPreview() {
  FreyzaEmployeeTheme {
    HomeScreen(
      HomeScreenUiState(
        UIState.Ready(dummyExpenses()),
        currentTravelPlan = UIState.Ready(dummyTravelPlan()),
        todayTravelPlanEntry = UIState.Ready(dummyTravelPlanEntryWork()),
        todayPlanEntryRoute = UIState.Ready(dummyRouteWithLocation()),
        currentDailyReport = UIState.Ready(null)
      ), MainUiState(
        hasValidSession = true, user = dummyUserEmployee(), today = LocalDateTime(
          year = 2026, month = Month.JANUARY, day = 1, hour = 5, minute = 59, second = 59
        )
      ),
      onRefresh = {},
      onDailyReportBegin = { dayType, routeId -> },
      onDailyReportRetry = {}
    )
  }
}
