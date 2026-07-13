package com.freyza.employee.presentation.ui.authenticated.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.timedGreeting
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dayTypes
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.AddVisitFloatingActionButton
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.FabActionItem
import com.freyza.employee.presentation.ui.authenticated.home.composables.BeginDailyReportSheet
import com.freyza.employee.presentation.ui.authenticated.home.composables.DailyReportCard
import com.freyza.employee.presentation.ui.authenticated.home.composables.DailyReportCardSkeleton
import com.freyza.employee.presentation.ui.authenticated.home.composables.HomeScreenSkeleton
import com.freyza.employee.presentation.ui.authenticated.home.composables.TodayPlanCard
import com.freyza.employee.presentation.ui.authenticated.home.composables.TravelPlanCardSkeleton
import com.freyza.employee.presentation.ui.composables.FreyzaHomeAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.LocalSnackbarHostState
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import com.freyza.employee.presentation.ui.state.dummyHomeScreenNoPlanUiState
import com.freyza.employee.presentation.ui.state.dummyHomeScreenNoReportUiState
import com.freyza.employee.presentation.ui.state.dummyHomeScreenUiState
import com.freyza.employee.presentation.ui.state.dummyHomeScreenUiStateDailyReportError
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
  onNavigateToReport: (reportId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
  onExit: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e("HomeScreenRoute", "Invalid User/Session on home screen")
    HomeScreenSkeleton(modifier = modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    HomeScreen(
      uiState = uiState,
      user = currentUser!!,
      modifier = modifier,
      onRefresh = viewModel::refresh,
      onLogout = onNavigateToUnauthenticated,
      onExit = onExit,
      onDailyReportBegin = viewModel::createCurrentDailyReport,
      onNavigateToReport = onNavigateToReport,
      onNavigateToAddVisit = onNavigateToAddVisit,
      onDismissSheet = viewModel::dismissCreateReportSheet
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
  uiState: HomeScreenUiState,
  user: User,
  modifier: Modifier = Modifier,
  onRefresh: () -> Unit,
  onLogout: () -> Unit,
  onExit: () -> Unit,
  onDailyReportBegin: (dayType: DayType, srcLocId: String?, destLocId: String?) -> Unit,
  onNavigateToReport: (reportId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
  onDismissSheet: () -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

  val reportCreationSheetState = rememberModalBottomSheetState(
    confirmValueChange = { newValue -> newValue != SheetValue.Hidden }, skipPartiallyExpanded = true
  )

  val fabOptions = listOf(
    FabActionItem(
      "Doctor Visit", VisitType.DOCTOR.iconResource()
    ) {
      if (uiState.currentDailyReport != null) onNavigateToAddVisit(
        VisitType.DOCTOR, uiState.currentDailyReport.id, uiState.currentDailyReport.employeeId
      )
    },
    FabActionItem(
      "Stockist Visit", VisitType.STOCKIST.iconResource()
    ) {
      if (uiState.currentDailyReport != null) onNavigateToAddVisit(
        VisitType.STOCKIST, uiState.currentDailyReport.id, uiState.currentDailyReport.employeeId
      )
    },
    FabActionItem(
      "Chemist Visit", VisitType.CHEMIST.iconResource()
    ) {
      if (uiState.currentDailyReport != null) onNavigateToAddVisit(
        VisitType.CHEMIST, uiState.currentDailyReport.id, uiState.currentDailyReport.employeeId
      )
    },
  )

  Scaffold(
    topBar = { FreyzaHomeAppBar(today = uiState.today, scrollBehavior = scrollBehavior) },
    floatingActionButton = {
      if (uiState.currentDailyReport != null && !uiState.currentDailyReport.locked && uiState.todayReportDayType == DayType.WORK) AddVisitFloatingActionButton(
        options = fabOptions
      )
    },
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
  ) {
    if (uiState.showCreateReportSheet) {
      ModalBottomSheet(
        onDismissRequest = onDismissSheet,
        sheetState = reportCreationSheetState,
        sheetGesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.75f),
      ) {
        Box {
          BeginDailyReportSheet(
            dayTypes = dayTypes,
            routes = uiState.routes,
            locations = uiState.locations,
            todayTravelPlanEntry = uiState.todayTravelPlanEntry,
            onDailyReportBegin = onDailyReportBegin,
            onRetry = onRefresh,
            onExit = onExit
          )
        }

        FreyzaSnackbarHost(
          hostState = LocalSnackbarHostState.current, modifier = Modifier.padding(bottom = 16.dp)
        )
      }
    }

    if (uiState.isLoading && uiState.currentDailyReport == null) {
      ModalBottomSheet(
        onDismissRequest = {},
        sheetState = reportCreationSheetState,
        scrimColor = BottomSheetDefaults.ScrimColor.copy(alpha = 0.85f),
        sheetGesturesEnabled = false,
        properties = ModalBottomSheetProperties(
          shouldDismissOnBackPress = false, shouldDismissOnClickOutside = false
        )
      ) {
        LoadingIndicator(
          Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.screen_padding).times(2)),
          "Preparing the daily report.."
        )
      }
    }

    if (uiState.errorMessage != null) {
      ErrorDialog(
        message = uiState.errorMessage, onRetry = onRefresh, onExit = onExit
      )
    }

    PullToRefreshBox(
      isRefreshing = uiState.isRefreshing,
      onRefresh = onRefresh,
      modifier = Modifier.padding(it)
    ) {
      LazyColumn(
        // inner screen padding to content
        contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing), Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxSize()
          // shrink the whole lazy column
          .imePadding()
      ) {
        item("greeting") {
          Text(
            uiState.today.timedGreeting(uiState.greetingName.ifEmpty { user.name }),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.default_spacing).times(2))
          )
        }

        // show travel plan entry only when report is null
        item("travelplan") {
          AnimatedVisibility(uiState.currentDailyReport == null, label = "travelplan") {
            if (uiState.todayTravelPlanEntry != null) {
              TodayPlanCard(
                planEntry = uiState.todayTravelPlanEntry,
                route = uiState.todayPlanEntryRoute,
                reportDayType = uiState.todayReportDayType,
                reportRoute = uiState.todayReportRoute,
                modifier = Modifier.fillMaxSize()
              )
            } else if (uiState.isLoading || uiState.isRefreshing) {
              TravelPlanCardSkeleton(
                modifier = Modifier.fillMaxSize()
              )
            } else {
              TodayPlanCard(
                planEntry = null, route = null,
                modifier = Modifier.fillMaxSize()
              )
            }
          }

          Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing)))
        }

        item("report_text") {
          Text(
            stringResource(R.string.today_report_header),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = dimensionResource(R.dimen.default_spacing))
              .padding(top = dimensionResource(R.dimen.default_spacing))
          )
        }

        item("daily_report") {
          if (uiState.currentDailyReport != null) {
            DailyReportCard(
              dailyReport = uiState.currentDailyReport,
              route = uiState.todayReportRoute,
              modifier = Modifier.fillMaxSize(),
              onClick = { onNavigateToReport(uiState.currentDailyReport.id) }
            )
          } else if (uiState.isLoading || uiState.isRefreshing) {
            DailyReportCardSkeleton(
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Button(
              onClick = onRefresh,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.default_spacing))
            ) {
              Text("Create Report")
            }
            Text(
              "Create a report to start making visits.",
              textAlign = TextAlign.Start,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        if (BuildConfig.DEBUG) {
          item {
            Spacer(Modifier.height(30.dp))
            DebugUiState(uiState)
          }

          item("debug_keyboard") {
            OutlinedTextField("", {})
          }
        }
      }
    }
  }
}

@Composable
private fun ErrorDialog(
  message: String?,
  onRetry: () -> Unit,
  onExit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    properties = DialogProperties(
      dismissOnBackPress = false, dismissOnClickOutside = false
    ),
    onDismissRequest = {},
    title = { Text("Please try again") },
    text = {
      Text(message ?: "Error occurred while loading the data")
    },
    confirmButton = {
      Button(onClick = onRetry) { Text("Retry") }
    },
    dismissButton = {
      TextButton(onClick = onExit) { Text("Exit") }
    },
    modifier = modifier,
  )
}

@Composable
private fun DebugUiState(uiState: HomeScreenUiState, modifier: Modifier = Modifier) {
  Card {
    Column(modifier = modifier.padding(8.dp)) {
      Text("isLoading: ${uiState.isLoading}")
      Text("isRefreshing: ${uiState.isRefreshing}")
      Text("errorMsg: ${uiState.errorMessage}")

      Text("travelPlan: ${uiState.currentTravelPlan?.id}")
      Text("planEntry: ${uiState.todayTravelPlanEntry?.id}")
      Text("planRoute: ${uiState.todayPlanEntryRoute?.id}")

      Text("dailyReport: ${uiState.currentDailyReport?.id}")
      Text("reportType: ${uiState.todayReportDayType}")
      Text("reportRoute: ${uiState.todayReportRoute?.id}")

      Text("routes.size: ${uiState.routes.size}")
      Text("showCreateReport: ${uiState.showCreateReportSheet}")
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  FreyzaEmployeeTheme {
    HomeScreen(
      uiState = dummyHomeScreenUiState(),
      user = dummyUserEmployee(),
      onRefresh = {},
      onLogout = {},
      onExit = {},
      onDailyReportBegin = { _, _, _ -> },
      onNavigateToReport = {},
      onNavigateToAddVisit = { _, _, _ -> },
      onDismissSheet = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNoPlanPreview() {
  FreyzaEmployeeTheme {
    HomeScreen(
      uiState = dummyHomeScreenNoPlanUiState(),
      user = dummyUserEmployee(),
      onRefresh = {},
      onLogout = {},
      onExit = {},
      onDailyReportBegin = { _, _, _ -> },
      onNavigateToReport = {},
      onNavigateToAddVisit = { _, _, _ -> },
      onDismissSheet = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNoReportPreview() {
  FreyzaEmployeeTheme {
    HomeScreen(
      uiState = dummyHomeScreenNoReportUiState(),
      user = dummyUserEmployee(),
      onRefresh = {},
      onLogout = {},
      onExit = {},
      onDailyReportBegin = { _, _, _ -> },
      onNavigateToReport = {},
      onNavigateToAddVisit = { _, _, _ -> },
      onDismissSheet = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenReportErrorPreview() {
  FreyzaEmployeeTheme {
    HomeScreen(
      uiState = dummyHomeScreenUiStateDailyReportError(),
      user = dummyUserEmployee(),
      onRefresh = {},
      onLogout = {},
      onExit = {},
      onDailyReportBegin = { _, _, _ -> },
      onNavigateToReport = {},
      onNavigateToAddVisit = { _, _, _ -> },
      onDismissSheet = {})
  }
}
