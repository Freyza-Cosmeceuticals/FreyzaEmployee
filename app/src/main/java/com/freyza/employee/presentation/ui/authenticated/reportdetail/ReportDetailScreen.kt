package com.freyza.employee.presentation.ui.authenticated.reportdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.core.util.toTitleCase
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.AddVisitFloatingActionButton
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.FabActionItem
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.LockReportButton
import com.freyza.employee.presentation.ui.authenticated.dailyreport.composables.VisitListItem
import com.freyza.employee.presentation.ui.composables.FreyzaReportDetailAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.ReportLockedBadge
import com.freyza.employee.presentation.ui.composables.RouteItem
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.ReportDetailUiState
import com.freyza.employee.presentation.ui.state.dummyReportDetailUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.ReportDetailViewModel
import org.koin.androidx.compose.koinViewModel

enum class VisitSortBy(val icon: Int) {
  TIME(R.drawable.sort_24px), NAME(R.drawable.sort_by_alpha_24px), TYPE(R.drawable.category_24px),
}

@Composable
fun ReportDetailScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: ReportDetailViewModel = koinViewModel(),
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateToVisitDetail: (visitId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e(
      "ReportDetailScreenRoute", "Invalid User/Session on report detail screen"
    )
    Skeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    ReportDetailScreen(
      uiState = uiState,
      user = currentUser!!,
      onRefresh = viewModel::refresh,
      onNavigateUp = onNavigateUp,
      onNavigateToVisitDetail = onNavigateToVisitDetail,
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
fun ReportDetailScreen(
  uiState: ReportDetailUiState,
  user: User,
  onRefresh: () -> Unit,
  onNavigateUp: () -> Unit,
  onNavigateToVisitDetail: (visitId: String) -> Unit,
  onNavigateToAddVisit: (type: VisitType, reportId: String, employeeId: String) -> Unit,
  onLockReport: (reportId: String) -> Unit,
  modifier: Modifier = Modifier,
  visitCreated: Boolean? = null,
  onVisitCreatedConsumed: () -> Unit = {},
) {

  val routeMap = remember(uiState.routes) {
    uiState.routes.associateBy { it.id }
  }

  val isToday = remember(uiState.report) {
    uiState.report.data?.date?.let { uiState.today.date == it } ?: false
  }

  var searchQuery by rememberSaveable { mutableStateOf("") }
  var filterTypes by rememberSaveable { mutableStateOf(setOf<VisitType>()) }
  var sortType by rememberSaveable { mutableStateOf(VisitSortBy.TIME) }
  var sortDesc by rememberSaveable { mutableStateOf(true) }

  val filteredVisits =
    remember(uiState.report.data?.visits, searchQuery, filterTypes, sortType, sortDesc) {
      uiState.report.data?.visits?.filter { visit ->
        val name = when (visit) {
          is Visit.DoctorVisit -> visit.doctorName
          is Visit.StockistVisit -> visit.stockistName
          is Visit.ChemistVisit -> visit.chemistName
        }

        val matchesName = name.contains(searchQuery, ignoreCase = true)
        val matchesAdditionalNotes =
          visit.additionalNotes?.contains(searchQuery, ignoreCase = true) ?: false

        val matchesSamplesDoctor = (visit as? Visit.DoctorVisit)?.samplesGiven?.any {
          it.contains(
            searchQuery, ignoreCase = true
          )
        } ?: false

        val matchesSampleStockist = (visit as? Visit.StockistVisit)?.samplesGiven?.any {
          it.contains(
            searchQuery, ignoreCase = true
          )
        } ?: false

        val matchesBillNo =
          (visit as? Visit.StockistVisit)?.billNo?.contains(searchQuery, ignoreCase = true) ?: false

        val matchesSearch =
          searchQuery.isBlank() || matchesName || matchesAdditionalNotes || matchesSamplesDoctor || matchesSampleStockist || matchesBillNo
        val matchesType = filterTypes.isEmpty() || visit.visitType in filterTypes

        matchesSearch && matchesType
      }?.let { list ->
        if (sortDesc) {
          when (sortType) {
            VisitSortBy.TIME -> list.sortedByDescending { it.createdAt }
            VisitSortBy.NAME -> list.sortedByDescending { visit ->
              when (visit) {
                is Visit.DoctorVisit -> visit.doctorName
                is Visit.StockistVisit -> visit.stockistName
                is Visit.ChemistVisit -> visit.chemistName
              }
            }

            VisitSortBy.TYPE -> list.sortedByDescending { it.visitType.name }
          }
        } else {
          when (sortType) {
            VisitSortBy.TIME -> list.sortedBy { it.createdAt }
            VisitSortBy.NAME -> list.sortedBy { visit ->
              when (visit) {
                is Visit.DoctorVisit -> visit.doctorName
                is Visit.StockistVisit -> visit.stockistName
                is Visit.ChemistVisit -> visit.chemistName
              }
            }

            VisitSortBy.TYPE -> list.sortedBy { it.visitType.name }
          }
        }
      } ?: emptyList()
    }

  val visitTypeCounts = remember(uiState.report.data?.visits) {
    derivedStateOf {
      mapOf(
        VisitType.DOCTOR to (uiState.report.data?.visits?.count { it.visitType == VisitType.DOCTOR }
          ?: 0),
        VisitType.STOCKIST to (uiState.report.data?.visits?.count { it.visitType == VisitType.STOCKIST }
          ?: 0),
        VisitType.CHEMIST to (uiState.report.data?.visits?.count { it.visitType == VisitType.CHEMIST }
          ?: 0)
      )
    }
  }

  val fabOptions = listOf(
    FabActionItem(
      "Doctor Visit", VisitType.DOCTOR.iconResource()
    ) {
      if (uiState.report.data != null) onNavigateToAddVisit(
        VisitType.DOCTOR, uiState.reportId, uiState.report.data.employeeId
      )
    },
    FabActionItem(
      "Stockist Visit", VisitType.STOCKIST.iconResource()
    ) {
      if (uiState.report.data != null) onNavigateToAddVisit(
        VisitType.STOCKIST, uiState.reportId, uiState.report.data.employeeId
      )
    },
    FabActionItem(
      "Chemist Visit", VisitType.CHEMIST.iconResource()
    ) {
      if (uiState.report.data != null) onNavigateToAddVisit(
        VisitType.CHEMIST, uiState.reportId, uiState.report.data.employeeId
      )
    },
  )

  Scaffold(
    topBar = { FreyzaReportDetailAppBar(navigateUp = onNavigateUp) },
    // only show add visit fab if there is some today report of type WORK
    floatingActionButton = {
      if (isToday && uiState.report.data != null && !uiState.report.data.locked && uiState.report.data.dayType == DayType.WORK) AddVisitFloatingActionButton(
        options = fabOptions
      )
    }) { paddingValues ->
    LaunchedEffect(visitCreated) {
      if (visitCreated != null) {
        onVisitCreatedConsumed()
      }
    }

    PullToRefreshBox(
      isRefreshing = uiState.report is UIState.Loading,
      onRefresh = onRefresh,
      modifier = Modifier
        .padding(paddingValues)
        .imePadding(),
    ) {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
        ),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
          .fillMaxSize()
          .padding(horizontal = dimensionResource(R.dimen.screen_padding))
      ) {
        when (val result = uiState.report) {
          is UIState.Ready -> {
            if (result.data == null) {
              item("404_text") {
                Text("Not Found")
              }
            } else {
              item("report_header") {
                Column(
                  modifier = Modifier.padding(
                    vertical = dimensionResource(R.dimen.default_spacing).times(2)
                  )
                ) {
                  ReportDetailHeader(
                    report = result.data, isToday = isToday, route = routeMap[result.data.routeId]
                  )

                  HorizontalDivider(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.default_spacing)),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                  )
                }
              }

              item("visit_heading") {
                Column(
                  modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.default_spacing).times(
                      2
                    )
                  ), verticalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.default_spacing).times(
                      3
                    )
                  )
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      "Visits".uppercase(),
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.secondary
                    )

                    if (result.data.visits.isNotEmpty()) {
                      Text(
                        "${result.data.visits.size} Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                      )
                    }
                  }

                  if (result.data.visits.isEmpty()) {
                    Text(
                      "No visits yet." + if (isToday) " Use the Add Visit button to add one." else "",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.outline
                    )
                  } else {
                    OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { searchQuery = it },
                      modifier = Modifier.fillMaxWidth(),
                      placeholder = { Text("Search...") },
                      leadingIcon = {
                        Icon(
                          painter = painterResource(R.drawable.search_24px),
                          contentDescription = null
                        )
                      },
                      trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                              Icon(
                                painter = painterResource(R.drawable.close_24px),
                                contentDescription = "Clear search"
                              )
                            }
                          }

                          var showSortMenu by remember { mutableStateOf(false) }
                          IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                              painter = painterResource(R.drawable.sort_24px),
                              contentDescription = "Sort",
                              tint = if (sortType != VisitSortBy.TIME || !sortDesc) {
                                MaterialTheme.colorScheme.primary
                              } else LocalContentColor.current,
                            )

                            DropdownMenu(
                              expanded = showSortMenu,
                              onDismissRequest = { showSortMenu = false }) {
                              VisitSortBy.entries.forEach { sortOption ->
                                DropdownMenuItem(text = {
                                  Text("Sort by ${sortOption.name.toTitleCase()}")
                                }, onClick = {
                                  if (sortType == sortOption) {
                                    sortDesc = !sortDesc
                                  } else {
                                    sortType = sortOption
                                    sortDesc = true
                                  }
                                  showSortMenu = false
                                }, leadingIcon = {
                                  Icon(
                                    painter = painterResource(sortOption.icon),
                                    contentDescription = null
                                  )
                                }, trailingIcon = {
                                  if (sortType == sortOption) {
                                    Icon(
                                      painter = painterResource(
                                        if (sortDesc) R.drawable.arrow_drop_down_24px else R.drawable.arrow_drop_up_24px
                                      ), contentDescription = null
                                    )
                                  }
                                })
                              }
                            }
                          }
                        }
                      },
                      singleLine = true,
                      shape = MaterialTheme.shapes.medium
                    )

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.default_spacing).times(
                          2
                        )
                      )
                    ) {
                      VisitType.entries.forEach { type ->
                        val isSelected = type in filterTypes
                        BadgedBox(badge = {
                          Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(visitTypeCounts.value[type].toString())
                          }
                        }) {
                          FilterChip(
                            selected = isSelected, onClick = {
                              filterTypes =
                                if (isSelected) filterTypes - type else filterTypes + type
                            }, label = { Text(type.titleCase()) }, leadingIcon = {
                              Icon(
                                painter = painterResource(type.iconResource()),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                              )
                            }, trailingIcon = if (isSelected) {
                              {
                                Icon(
                                  painter = painterResource(R.drawable.check_small_24px),
                                  contentDescription = null,
                                  modifier = Modifier.size(18.dp)
                                )
                              }
                            } else null)
                        }
                      }
                    }
                  }
                }
              }

              items(filteredVisits, key = { it.id }) { visit ->
                VisitListItem(
                  visit = visit,
                  modifier = Modifier
                    .padding(0.dp)
                    .clickable { onNavigateToVisitDetail(visit.id) })
              }

              if (isToday && result.data.locked.not()) {
                item("lock_button") {
                  LockReportButton(
                    lockingState = uiState.lockingState,
                    onLockPressed = { onLockReport(result.data.id) },
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.screen_padding))
                  )
                }
              }

              if (BuildConfig.DEBUG) {
                item(key = "report_debug") {
                  DebugDailyReport(result.data)
                }
              }
            }
          }

          is UIState.Loading -> {
            item("loading") {
              LoadingIndicator(
                Modifier
                  .fillMaxSize()
                  .padding(dimensionResource(R.dimen.screen_padding)),
                message = "Loading Report"
              )
            }
          }

          is UIState.Error -> {
            item("error_text") {
              Column(
                modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding).times(2)),
                verticalArrangement = Arrangement.spacedBy(
                  dimensionResource(R.dimen.default_spacing).times(2)
                ),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("Error Loading Report")

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
fun ReportDetailHeader(
  report: DailyReport,
  isToday: Boolean,
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing))
  ) {
    // Date & Lock Status
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing))
      ) {
        if (isToday) {
          Icon(
            painter = painterResource(R.drawable.calendar_month_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Text(
          text = if (isToday) "Today's Report" else DateFormatter.format(report.date),
          style = MaterialTheme.typography.titleMedium,
          color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
      }

      if (report.dayType == DayType.WORK) {
        ReportLockedBadge(report.locked)
      }
    }

    // Day Type & Expense
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = dimensionResource(R.dimen.screen_padding)),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = report.dayType.name,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.SemiBold
        )
        if (report.dayType == DayType.WORK) {
          RouteItem(route)
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = report.totalExpense.toCurrencyString(),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(
              3
            )
          )
        ) {
          Text(
            "TA: ${report.ta.toCurrencyString()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            "DA: ${report.da.toCurrencyString()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
          )
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

@Preview(showBackground = true)
@Composable
private fun ReportDetailHeaderTodayPreview() {
  FreyzaEmployeeTheme {
    ReportDetailHeader(dummyDailyReportWork(), isToday = true, dummyRouteWithLocation())
  }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailHeaderPreview() {
  FreyzaEmployeeTheme {
    ReportDetailHeader(dummyDailyReportWork(), isToday = false, dummyRouteWithLocation())
  }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailHeaderLockedPreview() {
  FreyzaEmployeeTheme {
    ReportDetailHeader(
      dummyDailyReportWork(locked = true), isToday = false, dummyRouteWithLocation()
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailScreenPreview() {
  FreyzaEmployeeTheme {
    ReportDetailScreen(
      dummyReportDetailUiState(),
      dummyUserEmployee(),
      {},
      {},
      {},
      { _, _, _ -> },
      {})
  }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailScreenTodayNoVisitsPreview() {
  FreyzaEmployeeTheme {
    ReportDetailScreen(
      dummyReportDetailUiState(dateNow = true, noVisits = true),
      dummyUserEmployee(),
      {},
      {},
      {},
      { _, _, _ -> },
      {})
  }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailScreenTodayPreview() {
  FreyzaEmployeeTheme {
    ReportDetailScreen(
      dummyReportDetailUiState(dateNow = true),
      dummyUserEmployee(),
      {},
      {},
      {},
      { _, _, _ -> },
      {})
  }
}
