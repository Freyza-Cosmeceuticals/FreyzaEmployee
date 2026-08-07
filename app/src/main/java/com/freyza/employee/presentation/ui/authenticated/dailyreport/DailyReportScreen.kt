package com.freyza.employee.presentation.ui.authenticated.dailyreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.AddVisitFloatingActionButton
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.DailyReportListCard
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.FabActionItem
import com.freyza.employee.presentation.ui.composables.FreyzaDailyReportAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.DailyReportUiState
import com.freyza.employee.presentation.ui.state.dummyDailyReportUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DailyReportScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: DailyReportViewModel = koinViewModel(),
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit,
  onNavigateToReportDetail: (reportId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e(
      "DailyReportScreenRoute", "Invalid User/Session on daily report screen"
    )
    Skeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    DailyReportScreen(
      uiState = uiState,
      user = currentUser!!,
      onRefresh = { viewModel.refresh(true) },
      modifier = modifier,
      visitCreated = visitCreated,
      onVisitCreatedConsumed = onVisitCreatedConsumed,
      onNavigateToReportDetail = onNavigateToReportDetail,
      onNavigateToAddVisit = onNavigateToAddVisit,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
  uiState: DailyReportUiState,
  user: User,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit = {},
  onNavigateToReportDetail: (reportId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
) {
  val todayReport = remember(uiState.dailyReports.data, uiState.today.date) {
    uiState.dailyReports.data?.firstOrNull { it.date == uiState.today.date }
  }
  val pastReports = remember(uiState.dailyReports.data, todayReport) {
    if (todayReport != null) uiState.dailyReports.data?.drop(1)
      ?: emptyList() else uiState.dailyReports.data ?: emptyList()
  }

  val routeMap = remember(uiState.routes) {
    uiState.routes.associateBy { it.id }
  }

  val employeeMap = remember(uiState.employees) {
    uiState.employees.associateBy { it.id }
  }

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
    floatingActionButton = {
      // only show add visit fab if there is some today report of type WORK
      if (todayReport != null && !todayReport.locked && todayReport.dayType == DayType.WORK) AddVisitFloatingActionButton(
        options = fabOptions
      )
    }
  ) {
    LaunchedEffect(visitCreated) {
      if (visitCreated != null) {
        onVisitCreatedConsumed()
      }
    }

    PullToRefreshBox(
      isRefreshing = uiState.dailyReports is UIState.Loading,
      onRefresh = onRefresh,
      modifier = modifier.padding(it),
    ) {
      LazyColumn(
        contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
          .fillMaxSize()
          .imePadding()
      ) {
        when (val result = uiState.dailyReports) {
          is UIState.Ready -> {
            if (todayReport != null) {
              item("today_report_${todayReport.id}") {
                DailyReportListCard(
                  report = todayReport,
                  route = routeMap[todayReport.routeId],
                  travellingWith = employeeMap[todayReport.travellingWithId],
                  isToday = true,
                  onClick = {
                    onNavigateToReportDetail(todayReport.id)
                  })
              }

              if (pastReports.isNotEmpty()) {
                item("separator") {
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
                travellingWith = employeeMap[report.travellingWithId],
                isToday = false,
                onClick = { onNavigateToReportDetail(report.id) })
            }

            if (BuildConfig.DEBUG) {
              result.data?.forEach {
                item("debug_${it.id}") {
                  DebugDailyReport(it)
                }
              }

              item("debug_keyboard") {
                OutlinedTextField("", {})
              }
            }
          }

          is UIState.Loading -> {
            item("loading") {
              LoadingIndicator(
                Modifier
                  .fillMaxSize()
                  .padding(dimensionResource(R.dimen.screen_padding)),
                message = "Loading Reports"
              )
            }
          }

          is UIState.Error -> {
            item("error_text") {
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

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportScreenPreview() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = dummyDailyReportUiState(),
      user = dummyUserEmployee(),
      onRefresh = {},
      onNavigateToReportDetail = { _ -> },
      onNavigateToAddVisit = { _, _, _ -> })
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportScreenPreviewLoading() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = DailyReportUiState(
        today = ServerTime().nowLocalDateTime(),
        dailyReports = UIState.Loading(null, "Cooking reports"),
        routes = listOf(dummyRouteWithLocation())
      ),
      user = dummyUserEmployee(),
      onRefresh = {},
      onNavigateToReportDetail = { _ -> },
      onNavigateToAddVisit = { _, _, _ -> })
  }
}


@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportScreenPreviewError() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = DailyReportUiState(
        today = ServerTime().nowLocalDateTime(),
        dailyReports = UIState.Error("Cannot to load reports"),
        routes = listOf(dummyRouteWithLocation())
      ),
      user = dummyUserEmployee(),
      onRefresh = {},
      onNavigateToReportDetail = { _ -> },
      onNavigateToAddVisit = { _, _, _ -> })
  }
}
