package com.freyza.employee.presentation.ui.authenticated.dailyreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.AddVisitFloatingActionButton
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.DailyReportDetailSheetContent
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.DailyReportListCard
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.FabActionItem
import com.freyza.employee.presentation.ui.composables.FreyzaDailyReportAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.LocalSnackbarHostState
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.DailyReportUiState
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.dummyDailyReportUiState
import com.freyza.employee.presentation.ui.state.dummyMainUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DailyReportScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: DailyReportViewModel = koinViewModel(),
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit,
  onNavigateToUnauthenticated: () -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
) {
  AuthenticatedRouteWrapper(
    mainUiState, onNavigateToUnauthenticated,
    loading = {
      Logger.e(
        "DailyReportScreenRoute", "Invalid User/Session on dailyreport screen, waiting for 5seconds"
      )
      Skeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
    },
    timeoutMillis = 5_000,
  ) { mainUiState ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DailyReportScreen(
      uiState = uiState,
      mainUiState = mainUiState,
      onRefresh = viewModel::refresh,
      onNavigateToAddVisit = onNavigateToAddVisit,
      onLockReport = viewModel::lockReport,
      modifier = modifier,
      visitCreated = visitCreated,
      onVisitCreatedConsumed = onVisitCreatedConsumed,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
  uiState: DailyReportUiState,
  mainUiState: MainUiState,
  onRefresh: () -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
  onLockReport: (reportId: String) -> Unit,
  modifier: Modifier = Modifier,
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit = {},
) {
  val sheetState = rememberModalBottomSheetState()

  val todayReport = remember(uiState.dailyReports.data, mainUiState.today.date) {
    uiState.dailyReports.data?.firstOrNull { it.date == mainUiState.today.date }
  }
  val pastReports = remember(uiState.dailyReports.data, todayReport) {
    if (todayReport != null) uiState.dailyReports.data?.drop(1)
      ?: emptyList() else uiState.dailyReports.data ?: emptyList()
  }

  val routeMap = remember(uiState.routes.data) {
    uiState.routes.data?.associateBy { it.id } ?: emptyMap()
  }

  var selectedReport by remember { mutableStateOf<DailyReport?>(null) }

  val fabOptions = listOf(
    FabActionItem(
      "Doctor Visit", VisitType.DOCTOR.iconResource()
    ) {
      if (todayReport != null) onNavigateToAddVisit(
        VisitType.DOCTOR, todayReport.id, todayReport.employeeId
      )
    },
    FabActionItem(
      "Stockist Visit", VisitType.STOCKIST.iconResource()
    ) {
      if (todayReport != null) onNavigateToAddVisit(
        VisitType.STOCKIST, todayReport.id, todayReport.employeeId
      )
    },
    FabActionItem(
      "Chemist Visit", VisitType.CHEMIST.iconResource()
    ) {
      if (todayReport != null) onNavigateToAddVisit(
        VisitType.CHEMIST, todayReport.id, todayReport.employeeId
      )
    },
  )

  Scaffold(
    topBar = { FreyzaDailyReportAppBar() },
    // only show add visit fab if there is some today report of type WORK
    floatingActionButton = {
      if (todayReport != null && !todayReport.locked && todayReport.dayType == DayType.WORK) AddVisitFloatingActionButton(
        options = fabOptions
      )
    },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) { paddingValues ->
    if (mainUiState.user == null) {
      return@Scaffold
    }

    LaunchedEffect(uiState.lockingState) {
      if (uiState.lockingState is UIState.Ready) {
        // close the sheet
        sheetState.hide()
        selectedReport = null
      }
    }

    selectedReport?.let {
      ModalBottomSheet(
        onDismissRequest = { selectedReport = null }, sheetState = sheetState
      ) {
        Box {
          DailyReportDetailSheetContent(
            selectedReport,
            routeMap[selectedReport!!.routeId],
            isToday = selectedReport?.id == todayReport?.id,
            lockingState = uiState.lockingState,
            onLockReport = onLockReport,
          )

          FreyzaSnackbarHost(
            hostState = LocalSnackbarHostState.current, modifier = Modifier
              .padding(bottom = 16.dp)
          )
        }
      }
    }

    LaunchedEffect(visitCreated) {
      if (visitCreated != null) {
        onVisitCreatedConsumed()
      }
    }

    PullToRefreshBox(
      isRefreshing = uiState.dailyReports is UIState.Loading,
      onRefresh = onRefresh,
      modifier = Modifier
        .padding(paddingValues)
        .imePadding(),
    ) {
      LazyColumn(
        contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
          .fillMaxSize()
          .padding(horizontal = dimensionResource(R.dimen.screen_padding))
      ) {
        when (val result = uiState.dailyReports) {
          is UIState.Ready -> {
            if (todayReport != null) {
              item(key = "today_report_${todayReport.id}") {
                DailyReportListCard(
                  report = todayReport,
                  route = routeMap[todayReport.routeId],
                  isToday = true,
                  onClick = {
                    // TODO: Open another route for editing this report instead
                    selectedReport = todayReport
                  })
              }

              if (pastReports.isNotEmpty()) {
                item(key = "separator") {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                  ) {
                    HorizontalDivider(Modifier.weight(1f))
                    Text(
                      text = "Past Reports",
                      modifier = Modifier.padding(horizontal = 12.dp),
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider(Modifier.weight(1f))
                  }
                }
              }
            }

            items(
              items = pastReports, key = { "report_${it.id}" }) { report ->
              DailyReportListCard(
                report = report,
                route = routeMap[report.routeId],
                isToday = false,
                onClick = { selectedReport = report })
            }

            if (BuildConfig.DEBUG) {
              result.data?.forEach {
                item(key = "debug_${it.id}") {
                  DebugDailyReport(it)
                }
              }
            }
          }

          is UIState.Loading -> {
            item {
              LoadingIndicator(
                Modifier
                  .fillMaxSize()
                  .padding(dimensionResource(R.dimen.screen_padding)),
                message = "Loading Reports"
              )
            }
          }

          is UIState.Error -> {
            item {
              Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding).times(2)),
                verticalArrangement = Arrangement.spacedBy(
                  dimensionResource(R.dimen.default_spacing).times(2)
                ),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("Error Loading Reports")

                FilledTonalButton(onClick = onRefresh) {
                  Text("Retry")
                }
              }
            }
          }

          else -> {}
        }
      }
    }
  }
}

@Composable
private fun DebugDailyReport(dailyReport: DailyReport?, modifier: Modifier = Modifier) {
  Card {
    Column(modifier = modifier.padding(8.dp)) {

      if (dailyReport === null) {
        Text("No DailyReport")
      } else {
        Text(dailyReport.id, fontFamily = FontFamily.Monospace)
        Text(dailyReport.employeeId)
        Text(DateFormatter.format(dailyReport.date))
        Text(dailyReport.dayType.toString())
        Text(dailyReport.routeId.toString())

        Text(dailyReport.da.toString())
        Text(dailyReport.ta.toString())
        Text(dailyReport.totalExpense.toString())

        Text(dailyReport.visits.size.toString())

        Text(dailyReport.locked.toString())
        Text(dailyReport.lockedAt.toString())

        Text(DateFormatter.format(dailyReport.createdAt))
        dailyReport.updatedAt?.let {
          Text(DateFormatter.format(it))
        }
      }
    }
  }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun DailyReportScreenPreview() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = dummyDailyReportUiState(),
      mainUiState = dummyMainUiState(),
      onRefresh = {},
      onNavigateToAddVisit = { type: VisitType, string: String, string1: String -> },
      onLockReport = {})
  }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun DailyReportScreenPreviewLoading() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = DailyReportUiState(
        dailyReports = UIState.Loading(null, "Cooking reports"),
        routes = UIState.Ready(listOf(dummyRouteWithLocation()))
      ),
      mainUiState = dummyMainUiState(),
      onRefresh = {},
      onNavigateToAddVisit = { type: VisitType, string: String, string1: String -> },
      onLockReport = {})
  }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun DailyReportScreenPreviewError() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = DailyReportUiState(
        dailyReports = UIState.Error("Cannot to load reports"),
        routes = UIState.Ready(listOf(dummyRouteWithLocation()))
      ),
      mainUiState = dummyMainUiState(),
      onRefresh = {},
      onNavigateToAddVisit = { type: VisitType, string: String, string1: String -> },
      onLockReport = {})
  }
}
