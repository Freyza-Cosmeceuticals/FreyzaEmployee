package com.freyza.employee.presentation.ui.authenticated.visitdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.presentation.ui.authenticated.visitdetail.composables.DeleteVisitButton
import com.freyza.employee.presentation.ui.authenticated.visitdetail.composables.DetailRow
import com.freyza.employee.presentation.ui.authenticated.visitdetail.composables.DetailSection
import com.freyza.employee.presentation.ui.composables.FreyzaVisitDetailAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.VisitDetailUiState
import com.freyza.employee.presentation.ui.state.dummyVisitDetailErrorUiState
import com.freyza.employee.presentation.ui.state.dummyVisitDetailUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.VisitDetailViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun VisitDetailScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: VisitDetailViewModel = koinViewModel(),
  onNavigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e(
      "VisitDetailScreenRoute", "Invalid User/Session on visit detail screen"
    )
    Skeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    VisitDetailScreen(
      uiState = uiState,
      today = viewModel.getToday(),
      onRefresh = viewModel::refresh,
      onNavigateUp = onNavigateUp,
      onDeleteVisit = { viewModel.deleteVisit(onNavigateUp) },
      modifier = modifier
    )
  }
}

@Composable
fun VisitDetailScreen(
  uiState: VisitDetailUiState,
  today: LocalDateTime,
  onRefresh: () -> Unit,
  onNavigateUp: () -> Unit,
  onDeleteVisit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = {
      FreyzaVisitDetailAppBar(
        visitType = uiState.visit.data?.visitType, navigateUp = onNavigateUp
      )
    }) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .imePadding()
    ) {
      when (val result = uiState.visit) {
        is UIState.Ready -> {
          val visit = result.data
          if (visit == null) {
            Text(
              "Visit not found",
              modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding))
            )
          } else {
            val report = uiState.report.data
            val isToday = report?.date?.let { it == today.date } ?: false
            val canDelete = isToday && !report.locked

            LazyColumn(
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
              verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.default_spacing).times(2)
              )
            ) {
              item("header") {
                VisitDetailHeader(visit)
              }

              item("specifics") {
                VisitSpecificDetails(visit)
              }

              if (!visit.additionalNotes.isNullOrBlank()) {
                item("notes") {
                  DetailSection(title = "Notes", icon = R.drawable.description_24px) {
                    Text(visit.additionalNotes!!, style = MaterialTheme.typography.bodyLarge)
                  }
                }
              }

              item("details") {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  val timeZone = TimeZone.of(Constants.TIMEZONE)
                  val createdTime =
                    DateFormatter.format(visit.createdAt.toLocalDateTime(timeZone).time)
                  Text(
                    text = "created at $createdTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                  )
                  if (visit.updatedAt != null && visit.updatedAt != visit.createdAt) {
                    val updatedTime =
                      DateFormatter.format(visit.updatedAt!!.toLocalDateTime(timeZone).time)
                    Text(
                      text = "last updated at $updatedTime",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.outline
                    )
                  }
                }
              }
            }

            if (BuildConfig.DEBUG) {
              OutlinedTextField("", {}, Modifier.fillMaxWidth())
            }

            // Bottom Actions
            if (canDelete) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(dimensionResource(R.dimen.screen_padding)),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                DeleteVisitButton(onDeleteVisit = onDeleteVisit, modifier = Modifier.weight(1f))
              }
            }
          }
        }

        is UIState.Loading -> {
          LoadingIndicator(
            Modifier
              .fillMaxSize()
              .padding(dimensionResource(R.dimen.screen_padding)),
            message = "Loading Visit"
          )
        }

        is UIState.Error -> {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(dimensionResource(R.dimen.screen_padding).times(2)),
            verticalArrangement = Arrangement.spacedBy(
              dimensionResource(R.dimen.default_spacing).times(2)
            ),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Error Loading Visit")

            FilledTonalButton(onClick = onRefresh) {
              Text("Retry")
            }
          }
        }

        else -> {}
      }
    }
  }
}

@Composable
private fun VisitDetailHeader(visit: Visit) {
  val name = when (visit) {
    is Visit.DoctorVisit -> visit.doctorName
    is Visit.StockistVisit -> visit.stockistName
    is Visit.ChemistVisit -> visit.chemistName
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = visit.visitType.name.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )

      val timeZone = TimeZone.of(Constants.TIMEZONE)
      Text(
        text = DateFormatter.format(visit.createdAt.toLocalDateTime(timeZone).date),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = name,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface
    )

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
  }
}

@Composable
private fun VisitSpecificDetails(visit: Visit) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    when (visit) {
      is Visit.DoctorVisit -> {
        DetailRow("Order Taken", if (visit.orderTaken) "Yes" else "No")
        if (visit.orderTaken) {
          DetailRow("Order Amount", visit.orderAmount.toCurrencyString())
        }
        DetailRow("Outstanding", visit.outstandingAmount.toCurrencyString())

        if (visit.productDetails.isNotEmpty()) {
          DetailSection(title = "Products", icon = R.drawable.inventory_2_24px) {
            visit.productDetails.forEach { product ->
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product.name, modifier = Modifier.weight(1f))
                Text(
                  "${Money(product.rate).toCurrencyString()} x ${product.quantity} = ${
                    Money(product.total).toCurrencyString()
                  }"
                )
              }
            }
          }
        }

        if (visit.samplesGiven.isNotEmpty()) {
          DetailSection(title = "Samples Given", icon = R.drawable.labs_24px) {
            Text(visit.samplesGiven.joinToString(", "))
          }
        }
      }

      is Visit.StockistVisit -> {
        DetailRow("Bill No", visit.billNo)
        DetailRow("Order Taken", if (visit.orderTaken) "Yes" else "No")
        DetailRow("Payment Collected", if (visit.paymentCollected) "Yes" else "No")
        DetailRow("Stock Checked", if (visit.stockChecked) "Yes" else "No")
        DetailRow("Amount (Excl. GST)", visit.amountWithoutGST.toCurrencyString())
        DetailRow("Amount (Incl. GST)", visit.amountWithGST.toCurrencyString())
        DetailRow("Outstanding", visit.outstandingAmount.toCurrencyString())

        if (visit.samplesGiven.isNotEmpty()) {
          DetailSection(title = "Samples Given", icon = R.drawable.labs_24px) {
            Text(visit.samplesGiven.joinToString(", "))
          }
        }
      }

      is Visit.ChemistVisit -> {
        DetailRow("Order Taken", if (visit.orderTaken) "Yes" else "No")
        DetailRow("Outstanding", visit.outstandingAmount.toCurrencyString())
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun VisitDetailScreenPreview() {
  FreyzaEmployeeTheme {
    VisitDetailScreen(
      uiState = dummyVisitDetailUiState(),
      today = ServerTime().nowLocalDateTime(),
      onRefresh = {},
      onNavigateUp = {},
      onDeleteVisit = {})
  }
}


@Preview(showBackground = true)
@Composable
private fun VisitDetailScreenErrorPreview() {
  FreyzaEmployeeTheme {
    VisitDetailScreen(
      uiState = dummyVisitDetailErrorUiState(),
      today = ServerTime().nowLocalDateTime(),
      onRefresh = {},
      onNavigateUp = {},
      onDeleteVisit = {})
  }
}
