package com.freyza.employee.presentation.ui.authenticated.addvisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.BillingDetailsCard
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.BottomStatusBar
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.ClientInfoCard
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.NotesCard
import com.freyza.employee.presentation.ui.authenticated.addvisit.composables.OrderDetailsCard
import com.freyza.employee.presentation.ui.composables.FreyzaAddVisitAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import com.freyza.employee.presentation.ui.state.ProductEntry
import com.freyza.employee.presentation.ui.state.dummyAddVisitFormChemist
import com.freyza.employee.presentation.ui.state.dummyAddVisitFormDoctor
import com.freyza.employee.presentation.ui.state.dummyAddVisitFormStockist
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddVisitScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: AddVisitViewModel = koinViewModel(),
  onNavigateUp: (created: Boolean?) -> Unit,
  onNavigateToUnauthenticated: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e(
      "AddVisitScreenRoute", "Invalid User/Session on add visit screen"
    )
    Skeleton(modifier = modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    AddVisitScreen(
      uiState = uiState,
      user = currentUser!!,
      isEdit = viewModel.isEditMode,
      onNavigateUp = onNavigateUp,
      onRetry = viewModel::refresh,
      onNameChange = viewModel::updateName,
      onPoiSelect = viewModel::selectPoi,
      onNotesChange = viewModel::updateNotes,
      onSamplesChange = viewModel::updateSamplesGiven,
      onOrderTakenChange = viewModel::updateOrderTaken,
      onProductUpdate = viewModel::updateProductEntry,
      onAddProduct = viewModel::addProductEntry,
      onRemoveProduct = viewModel::removeProductEntry,
      onOutstandingAmountChange = viewModel::updateOutstandingAmount,
      onBillNoChange = viewModel::updateBillNo,
      onPaymentCollectedChange = viewModel::updatePaymentCollected,
      onAmountWithGSTChange = viewModel::updateAmountWithGST,
      onAmountWithoutGSTChange = viewModel::updateAmountWithoutGST,
      onStockCheckedChange = viewModel::updateStockChecked,
      onSubmitVisit = viewModel::submitVisit,
      modifier = modifier
    )
  }
}

@Composable
fun AddVisitScreen(
  uiState: AddVisitUiState,
  user: User,
  onNavigateUp: (created: Boolean?) -> Unit,
  onRetry: () -> Unit,
  onNameChange: (String) -> Unit,
  onPoiSelect: (PointOfInterest?) -> Unit,
  onNotesChange: (String) -> Unit,
  onSamplesChange: (List<String>) -> Unit,
  onOrderTakenChange: (Boolean) -> Unit,
  onProductUpdate: (Int, ProductEntry) -> Unit,
  onAddProduct: () -> Unit,
  onRemoveProduct: (Int) -> Unit,
  onOutstandingAmountChange: (String) -> Unit,
  onBillNoChange: (String) -> Unit,
  onPaymentCollectedChange: (Boolean) -> Unit,
  onAmountWithGSTChange: (String) -> Unit,
  onAmountWithoutGSTChange: (String) -> Unit,
  onStockCheckedChange: (Boolean) -> Unit,
  onSubmitVisit: () -> Unit,
  modifier: Modifier = Modifier,
  isEdit: Boolean = false,
) {
  val form = uiState.form
  val focusManager = LocalFocusManager.current

  Scaffold(
    topBar = {
      FreyzaAddVisitAppBar(
        uiState.visitType,
        navigateUp = { onNavigateUp(if (uiState.creationState is UIState.Error) false else null) },
        isEdit = isEdit
      )
    }, bottomBar = {
      if (uiState.visitType != null) BottomStatusBar(
        visitType = uiState.visitType,
        orderTaken = form.orderTaken,
        orderAmount = form.orderAmount,
        numProducts = form.numProducts,
        totalQuantity = form.totalQuantity,
        creationState = uiState.creationState,
        onSubmitVisit = onSubmitVisit,
        modifier = Modifier.fillMaxWidth()
      )
    },
    // handle IME here for bottom bar
    modifier = modifier.imePadding()
  ) { paddingValues ->
    // handle screen transitions and error states
    LaunchedEffect(uiState.creationState) {
      if (uiState.creationState is UIState.Ready) {
        onNavigateUp(true)
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        // ime is taken by the scaffold itself for local bottom bar
        .padding(paddingValues)
    ) {
      if (uiState.visitType == null) {
        item("loading") {
          LoadingIndicator(Modifier.fillMaxSize(), message = "Loading form...")
        }
        return@LazyColumn
      }

      item("client_info") {
        ClientInfoCard(
          visitType = uiState.visitType,
          name = form.name,
          onNameChange = onNameChange,
          availablePois = uiState.availablePois.data ?: emptyList(),
          selectedPoiId = form.poiId,
          onPoiSelect = onPoiSelect,
          samplesGiven = form.samplesGiven,
          onSamplesChange = onSamplesChange,
          focusManager = focusManager,
          enabled = (uiState.creationState is UIState.Idle || uiState.creationState is UIState.Error) &&
                  uiState.availablePois !is UIState.Loading
        )
      }

      item("order_details") {
        OrderDetailsCard(
          visitType = uiState.visitType,
          orderTaken = form.orderTaken,
          onOrderTakenChange = onOrderTakenChange,
          productEntries = form.productEntries,
          onProductUpdate = onProductUpdate,
          onAddProduct = onAddProduct,
          onRemoveProduct = onRemoveProduct,
          focusManager = focusManager,
          enabled = uiState.creationState is UIState.Idle || uiState.creationState is UIState.Error
        )
      }

      item("billing_details") {
        BillingDetailsCard(
          visitType = uiState.visitType,
          outstandingAmount = form.outstandingAmount,
          onOutstandingAmountChange = onOutstandingAmountChange,
          billNo = form.billNo,
          onBillNoChange = onBillNoChange,
          stockChecked = form.stockChecked,
          onStockCheckedChange = onStockCheckedChange,
          paymentCollected = form.paymentCollected,
          onPaymentCollectedChange = onPaymentCollectedChange,
          amountWithGST = form.amountWithGST,
          onAmountWithGSTChange = onAmountWithGSTChange,
          amountWithoutGST = form.amountWithoutGST,
          onAmountWithoutGSTChange = onAmountWithoutGSTChange,
          focusManager = focusManager,
          enabled = uiState.creationState is UIState.Idle || uiState.creationState is UIState.Error
        )
      }

      item("notes") {
        NotesCard(
          notes = form.notes,
          onNotesChange = onNotesChange,
          focusManager = focusManager,
          enabled = uiState.creationState is UIState.Idle || uiState.creationState is UIState.Error
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun AddVisitScreenPreviewDoctor() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(
        today = ServerTime().nowLocalDateTime(),
        visitType = VisitType.DOCTOR,
        form = dummyAddVisitFormDoctor()
      ),
      user = dummyUserEmployee(),
      onNavigateUp = {},
      onRetry = {},
      onNameChange = {},
      onPoiSelect = {},
      onNotesChange = {},
      onSamplesChange = {},
      onOrderTakenChange = {},
      onProductUpdate = { _, _ -> },
      onAddProduct = {},
      onRemoveProduct = {},
      onOutstandingAmountChange = {},
      onBillNoChange = {},
      onPaymentCollectedChange = {},
      onAmountWithGSTChange = {},
      onAmountWithoutGSTChange = {},
      onStockCheckedChange = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun AddVisitScreenPreviewStockist() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(
        today = ServerTime().nowLocalDateTime(),
        visitType = VisitType.STOCKIST,
        form = dummyAddVisitFormStockist()
      ),
      user = dummyUserEmployee(),
      onNavigateUp = {},
      onRetry = {},
      onNameChange = {},
      onPoiSelect = {},
      onNotesChange = {},
      onSamplesChange = {},
      onOrderTakenChange = {},
      onProductUpdate = { _, _ -> },
      onAddProduct = {},
      onRemoveProduct = {},
      onOutstandingAmountChange = {},
      onBillNoChange = {},
      onPaymentCollectedChange = {},
      onAmountWithGSTChange = {},
      onAmountWithoutGSTChange = {},
      onStockCheckedChange = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun AddVisitScreenPreviewChemist() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(
        today = ServerTime().nowLocalDateTime(),
        visitType = VisitType.CHEMIST,
        form = dummyAddVisitFormChemist()
      ),
      user = dummyUserEmployee(),
      onNavigateUp = {},
      onRetry = {},
      onNameChange = {},
      onPoiSelect = {},
      onNotesChange = {},
      onSamplesChange = {},
      onOrderTakenChange = {},
      onProductUpdate = { _, _ -> },
      onAddProduct = {},
      onRemoveProduct = {},
      onOutstandingAmountChange = {},
      onBillNoChange = {},
      onPaymentCollectedChange = {},
      onAmountWithGSTChange = {},
      onAmountWithoutGSTChange = {},
      onStockCheckedChange = {},
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun AddVisitScreenPreviewNull() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(
        today = ServerTime().nowLocalDateTime(), visitType = null
      ),
      user = dummyUserEmployee(),
      onNavigateUp = {},
      onRetry = {},
      onNameChange = {},
      onPoiSelect = {},
      onNotesChange = {},
      onSamplesChange = {},
      onOrderTakenChange = {},
      onProductUpdate = { _, _ -> },
      onAddProduct = {},
      onRemoveProduct = {},
      onOutstandingAmountChange = {},
      onBillNoChange = {},
      onPaymentCollectedChange = {},
      onAmountWithGSTChange = {},
      onAmountWithoutGSTChange = {},
      onStockCheckedChange = {},
      onSubmitVisit = {})
  }
}
