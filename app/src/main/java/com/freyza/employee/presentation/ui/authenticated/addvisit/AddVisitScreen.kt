package com.freyza.employee.presentation.ui.authenticated.addvisit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.composables.FreyzaAddVisitAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.dummyMainUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddVisitScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: AddVisitViewModel = koinViewModel(),
  onNavigateUp: () -> Unit,
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
      modifier = modifier
    )
  }
}

@Composable
fun AddVisitScreen(
  uiState: AddVisitUiState,
  mainUiState: MainUiState,
  onNavigateUp: () -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    topBar = { FreyzaAddVisitAppBar(uiState.visitType, onNavigateUp) },
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
      item {
        Text("Add Visit ${uiState.visitType}")
      }
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddVisitScreenPreview() {
  FreyzaEmployeeTheme {
    AddVisitScreen(
      uiState = AddVisitUiState(visitType = VisitType.DOCTOR),
      mainUiState = dummyMainUiState(),
      onNavigateUp = {},
      onRetry = {}
    )
  }
}
