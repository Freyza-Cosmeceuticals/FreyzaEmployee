package com.freyza.employee.presentation.ui.authenticated.visitdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.presentation.ui.composables.FreyzaVisitDetailAppBar
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.VisitDetailUiState
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
  onNavigateUp: () -> Unit,
  onDeleteVisit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      FreyzaVisitDetailAppBar(
        visitType = uiState.visit.data?.visitType, navigateUp = onNavigateUp
      )
    }) { paddingValues ->
    Column(
      modifier = modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      when (val result = uiState.visit) {
        is UIState.Ready -> {
          val visit = result.data
          if (visit == null) {
            Text("Visit not found", modifier = Modifier.padding(16.dp))
          } else {
            val report = uiState.report.data
            val isToday = report?.date?.let { it == today.date } ?: false
            val canDelete = isToday && report.locked == false

            LazyColumn(
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
              verticalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.default_spacing).times(
                  2
                )
              )
            ) {
              item {
                VisitDetailHeader(visit)
              }

              item {
                VisitSpecificDetails(visit)
              }

              if (!visit.additionalNotes.isNullOrBlank()) {
                item {
                  DetailSection(title = "Notes", icon = R.drawable.inventory_2_24px) {
                    Text(visit.additionalNotes!!, style = MaterialTheme.typography.bodyLarge)
                  }
                }
              }

              item {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  val timeZone = TimeZone.of(com.freyza.employee.core.Constants.TIMEZONE)
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

            // Bottom Actions
            if (canDelete) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(dimensionResource(R.dimen.screen_padding)),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                OutlinedButton(
                  onClick = { showDeleteDialog = true },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                  Icon(painterResource(R.drawable.close_24px), contentDescription = null)
                  Text("Delete Visit", modifier = Modifier.padding(start = 8.dp))
                }
              }
            }
          }
        }

        is UIState.Loading -> {
          Skeleton(
            Modifier
              .fillMaxSize()
              .padding(16.dp)
          )
        }

        is UIState.Error -> {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Error: ${result.message}")
            Button(onClick = onNavigateUp) { Text("Go Back") }
          }
        }

        else -> {}
      }
    }
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Visit") },
      text = { Text("Are you sure you want to delete this visit? This action cannot be undone.") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteDialog = false
            onDeleteVisit()
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      })
  }
}

@Composable
fun VisitDetailHeader(visit: Visit) {
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
        text = visit.visitType.titleCase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )

      val timeZone = TimeZone.of(com.freyza.employee.core.Constants.TIMEZONE)
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
fun VisitSpecificDetails(visit: Visit) {
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

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
  }
}

@Composable
fun DetailSection(title: String, icon: Int, content: @Composable () -> Unit) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary
        )
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
      content()
    }
  }
}

@Preview(showBackground = true)
@Composable
fun VisitDetailScreenPreview() {
  FreyzaEmployeeTheme {
    VisitDetailScreen(
      uiState = dummyVisitDetailUiState(),
      today = ServerTime().nowLocalDateTime(),
      onNavigateUp = {},
      onDeleteVisit = {})
  }
}
