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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.freyza.employee.data.mappers.toProductDetail
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
import com.freyza.employee.presentation.ui.state.AddVisitFormState
import com.freyza.employee.presentation.ui.state.AddVisitUiState
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
      onNavigateUp = onNavigateUp,
      onRetry = viewModel::refresh,
      onFormUpdate = viewModel::updateForm,
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
  onFormUpdate: (AddVisitFormState) -> Unit,
  onSubmitVisit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val form = uiState.form
  val focusManager = LocalFocusManager.current

  val orderStats by remember(form.productEntries) {
    derivedStateOf {
      var count = 0
      var quantity = 0

      form.productEntries.forEach { entry ->
        entry.toProductDetail()?.let { detail ->
          count++
          quantity += detail.quantity
        }
      }

      Pair(count, quantity)
    }
  }

  Scaffold(
    topBar = {
    FreyzaAddVisitAppBar(
      uiState.visitType,
      navigateUp = { onNavigateUp(if (uiState.creationState is UIState.Error) false else null) })
  }, bottomBar = {
    if (uiState.visitType != null) BottomStatusBar(
      visitType = uiState.visitType,
      orderTaken = form.orderTaken,
      orderAmount = form.orderAmount,
      numProducts = orderStats.first,
      totalQuantity = orderStats.second,
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
          form = form,
          onFormUpdate = onFormUpdate,
          focusManager = focusManager
        )
      }

      item("order_details") {
        OrderDetailsCard(
          visitType = uiState.visitType,
          form = form,
          onFormUpdate = onFormUpdate,
          focusManager = focusManager
        )
      }

      item("billing_details") {
        BillingDetailsCard(
          visitType = uiState.visitType,
          form = form,
          onFormUpdate = onFormUpdate,
          focusManager = focusManager
        )
      }

      item("notes") {
        NotesCard(
          notes = form.notes,
          onNotesChange = { onFormUpdate(form.copy(notes = it)) },
          focusManager = focusManager
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
      onFormUpdate = {},
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
      onFormUpdate = {},
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
      onFormUpdate = {},
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
      onFormUpdate = {},
      onSubmitVisit = {})
  }
}
