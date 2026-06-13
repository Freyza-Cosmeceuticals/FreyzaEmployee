package com.freyza.employee.presentation.ui.authenticated.addvisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.ProductDetail
import com.freyza.employee.domain.model.VisitCreate
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.TagInputField
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.ToggleableRow
import com.freyza.employee.presentation.ui.composables.FreyzaAddVisitAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.dummyMainUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import org.koin.androidx.compose.koinViewModel

data class ProductEntry(
  val name: String = "",
  val rate: String = "",
  val quantity: String = "",
)

@Composable
fun AddVisitScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: AddVisitViewModel = koinViewModel(),
  onNavigateUp: (created: Boolean?) -> Unit,
  onNavigateToUnauthenticated: () -> Unit,
) {
  AuthenticatedRouteWrapper(
    mainUiState, onNavigateToUnauthenticated,
    loading = {
      Logger.e(
        "AddVisitScreenRoute", "Invalid User/Session on addvisit screen, waiting for 5seconds"
      )
      Skeleton(modifier = modifier.padding(dimensionResource(R.dimen.screen_padding)))
    },
    timeoutMillis = 5_000,
  ) { mainUiState ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddVisitScreen(
      uiState = uiState,
      mainUiState = mainUiState,
      onNavigateUp = onNavigateUp,
      onRetry = viewModel::refresh,
      onSubmitVisit = viewModel::submitVisit,
      modifier = modifier
    )
  }
}

@Composable
fun AddVisitScreen(
  uiState: AddVisitUiState,
  mainUiState: MainUiState,
  onNavigateUp: (created: Boolean?) -> Unit,
  onRetry: () -> Unit,
  onSubmitVisit: (visitCreate: VisitCreate) -> Unit,
  modifier: Modifier = Modifier,
) {
  var notes by rememberSaveable { mutableStateOf<String?>(null) }

  // Doctor / Chemist specific
  var doctorName by rememberSaveable { mutableStateOf<String?>(null) }
  val productEntries = remember { mutableStateListOf<ProductEntry>() }
  var samplesGiven by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
  var orderTaken by rememberSaveable { mutableStateOf(false) }

  // Stockist specific
  var stockistName by rememberSaveable { mutableStateOf<String?>(null) }
  var billNo by rememberSaveable { mutableStateOf<String?>(null) }
  var paymentCollected by rememberSaveable { mutableStateOf(false) }
  var amountWithGST by rememberSaveable { mutableDoubleStateOf(0.00) }
  var amountWithoutGST by rememberSaveable { mutableDoubleStateOf(0.00) }
  var stockChecked by rememberSaveable { mutableStateOf(false) }

  var chemistName by rememberSaveable { mutableStateOf<String?>(null) }
  var outstandingAmount by rememberSaveable { mutableDoubleStateOf(0.00) }

  val orderAmount by remember {
    derivedStateOf {
      productEntries.sumOf {
        (it.rate.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 0)
      }
    }
  }

  Scaffold(
    topBar = {
      FreyzaAddVisitAppBar(
        uiState.visitType,
        navigateUp = { onNavigateUp(if (uiState.creationState is UIState.Error) false else null) })
    },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) { paddingValues ->
    if (mainUiState.user == null) {
      return@Scaffold
    }

    // handle screen transitions and error states
    LaunchedEffect(uiState.creationState) {
      if (uiState.creationState is UIState.Ready) {
        onNavigateUp(true)
      }
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
        .imePadding()
    ) {
      if (uiState.visitType == null) {
        item {
          LoadingIndicator(Modifier.fillMaxSize(), message = "Loading form...")
        }

        return@LazyColumn
      }

      // Name
      item {
        val (nameValue, onNameChange, label) = when (uiState.visitType) {
          VisitType.DOCTOR -> Triple(
            doctorName ?: "",
            { it: String -> doctorName = it },
            "Doctor Name"
          )

          VisitType.STOCKIST -> Triple(
            stockistName ?: "",
            { it: String -> stockistName = it },
            "Stockist Name"
          )

          VisitType.CHEMIST -> Triple(
            chemistName ?: "",
            { it: String -> chemistName = it },
            "Chemist Name"
          )
        }

        OutlinedTextField(
          value = nameValue,
          onValueChange = { onNameChange(it) },
          label = { Text(label) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }

      // common between doctor and stockist
      if (uiState.visitType == VisitType.DOCTOR || uiState.visitType == VisitType.STOCKIST) {
        item {
          TagInputField(
            items = samplesGiven,
            onItemAdded = { if (!samplesGiven.contains(it)) samplesGiven = samplesGiven + it },
            onItemRemoved = { samplesGiven = samplesGiven - it },
            label = "Samples Given"
          )
        }
      }

      // common between all
      item {
        ToggleableRow(
          checked = orderTaken,
          onCheckedChange = { orderTaken = it },
          text = "Order Taken?"
        )
      }

      // products list for Doctor
      if (orderTaken && uiState.visitType == VisitType.DOCTOR) {
        productEntries.forEachIndexed { index, product ->
          item(key = "product_$index") {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedTextField(
                value = product.name,
                onValueChange = { productEntries[index] = product.copy(name = it) },
                label = { Text("Product") },
                modifier = Modifier.weight(1.5f),
                singleLine = true
              )
              OutlinedTextField(
                value = product.rate,
                onValueChange = { productEntries[index] = product.copy(rate = it) },
                label = { Text("Rate") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
              )
              OutlinedTextField(
                value = product.quantity,
                onValueChange = { productEntries[index] = product.copy(quantity = it) },
                label = { Text("Qty") },
                modifier = Modifier.weight(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
              )
              IconButton(onClick = { productEntries.removeAt(index) }) {
                Icon(
                  painter = painterResource(R.drawable.backspace_24px),
                  contentDescription = "Remove Product",
                  tint = MaterialTheme.colorScheme.error
                )
              }
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(onClick = {
              productEntries.add(ProductEntry())
            }) {
              Text("+ Add product")
            }

            Text(
              text = "Total: ₹${"%.2f".format(orderAmount)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      item {
        var outstandingStr by rememberSaveable { mutableStateOf(if (outstandingAmount == 0.0) "" else outstandingAmount.toString()) }
        OutlinedTextField(
          value = outstandingStr,
          onValueChange = {
            outstandingStr = it
            outstandingAmount = it.toDoubleOrNull() ?: 0.0
          },
          label = { Text("Current Outstanding Amount") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true
        )
      }

      // for stockist
      if (uiState.visitType == VisitType.STOCKIST) {
        item {
          OutlinedTextField(
            value = billNo ?: "",
            onValueChange = { billNo = it },
            label = { Text("Bill Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          ToggleableRow(
            checked = stockChecked,
            onCheckedChange = { stockChecked = it },
            text = "Stock Checked?"
          )
        }
      }

      if (uiState.visitType == VisitType.STOCKIST || uiState.visitType == VisitType.CHEMIST) {
        item {
          ToggleableRow(
            checked = paymentCollected,
            onCheckedChange = { paymentCollected = it },
            text = "Payment Collected?"
          )
        }

        if (paymentCollected) {
          item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              var amtWithoutGstStr by rememberSaveable { mutableStateOf(if (amountWithoutGST == 0.0) "" else amountWithoutGST.toString()) }
              var amtWithGstStr by rememberSaveable { mutableStateOf(if (amountWithGST == 0.0) "" else amountWithGST.toString()) }

              OutlinedTextField(
                value = amtWithoutGstStr,
                onValueChange = {
                  amtWithoutGstStr = it
                  amountWithoutGST = it.toDoubleOrNull() ?: 0.0
                },
                label = { Text("W/O GST") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
              )
              OutlinedTextField(
                value = amtWithGstStr,
                onValueChange = {
                  amtWithGstStr = it
                  amountWithGST = it.toDoubleOrNull() ?: 0.0
                },
                label = { Text("With GST") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
              )
            }
          }
        }
      }

      // 3. Shared Notes Field
      item {
        OutlinedTextField(
          value = notes ?: "",
          onValueChange = { notes = it },
          label = { Text("Additional Notes") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          maxLines = 5
        )
      }

      item {
        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(8)))

        Button(
          onClick = {
            onSubmitVisit(
              VisitCreate(
                doctorName = doctorName,
                stockistName = stockistName,
                chemistName = chemistName,
                productDetails = productEntries.filter { it.name.isNotBlank() }.map {
                  ProductDetail(
                    name = it.name,
                    rate = it.rate.toDoubleOrNull() ?: 0.0,
                    quantity = it.quantity.toIntOrNull() ?: 0
                  )
                },
                samplesGiven = samplesGiven,
                orderTaken = orderTaken,
                billNo = billNo,
                paymentCollected = paymentCollected,
                amountWithGST = amountWithGST,
                amountWithoutGST = amountWithoutGST,
                outstandingAmount = outstandingAmount,
                orderAmount = if (uiState.visitType == VisitType.DOCTOR) orderAmount else null,
                stockChecked = stockChecked,
                notes = notes
              )
            )
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = (uiState.creationState is UIState.Idle) || (uiState.creationState is UIState.Error)
        ) {
          if (uiState.creationState is UIState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
          } else {
            Text("Save Visit")
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddVisitScreenPreviewDoctor() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(visitType = VisitType.DOCTOR),
      mainUiState = dummyMainUiState(),
      onNavigateUp = {},
      onRetry = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddVisitScreenPreviewStockist() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(visitType = VisitType.STOCKIST),
      mainUiState = dummyMainUiState(),
      onNavigateUp = {},
      onRetry = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddVisitScreenPreviewChemist() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(visitType = VisitType.CHEMIST),
      mainUiState = dummyMainUiState(),
      onNavigateUp = {},
      onRetry = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddVisitScreenPreviewNull() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(visitType = null),
      mainUiState = dummyMainUiState(),
      onNavigateUp = {},
      onRetry = {},
      onSubmitVisit = {})
  }
}
