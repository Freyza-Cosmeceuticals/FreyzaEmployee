package com.freyza.employee.presentation.ui.authenticated.dailyreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.DailyReportListCard
import com.freyza.employee.presentation.ui.composables.FreyzaDailyReportAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
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
  onNavigateToUnauthenticated: () -> Unit,
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
      uiState, mainUiState, modifier
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
  uiState: DailyReportUiState,
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val todayReport = remember(uiState.dailyReports.data, mainUiState.today?.date) {
    uiState.dailyReports.data?.firstOrNull { it.date == mainUiState.today?.date }
  }
  val pastReports = remember(uiState.dailyReports.data, todayReport) {
    if (todayReport != null) uiState.dailyReports.data?.drop(1)
      ?: emptyList() else uiState.dailyReports.data ?: emptyList()
  }
  val routeMap = remember(uiState.routes.data) {
    uiState.routes.data?.associateBy { it.id } ?: emptyMap()
  }

  Scaffold(
    topBar = { FreyzaDailyReportAppBar() },
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) { paddingValues ->
    if (mainUiState.user == null || mainUiState.today == null) {
      return@Scaffold
    }

    LazyColumn(
      contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.screen_padding)),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
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
                onClick = { }
              )
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
            items = pastReports,
            key = { it.id }
          ) { report ->
            DailyReportListCard(
              report = report,
              route = routeMap[report.routeId],
              isToday = false,
              onClick = { }
            )
          }

          if (BuildConfig.DEBUG) {
            result.data?.forEach {
              item {
                DebugDailyReport(it)
              }
            }
          }
        }

        is UIState.Loading -> {
          item {
            LoadingIndicator(Modifier.fillMaxSize(), message = "Loading Reports...")
          }
        }

        is UIState.Error -> {
          item {
            Text("Error Loading Reports")
          }
        }

        else -> {}
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

@Preview
@Composable
private fun DailyReportScreenPreview() {
  FreyzaEmployeeTheme {
    DailyReportScreen(
      uiState = dummyDailyReportUiState(),
      mainUiState = dummyMainUiState()
    )
  }
}
