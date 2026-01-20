package com.freyza.employee.presentation.ui.authenticated.travelplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.FreyzaTpAppBar
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.TravelPlanUiState
import com.freyza.employee.presentation.ui.viewmodels.MainViewModel
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TravelPlanScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: TravelPlanViewModel = koinViewModel(),
  mainViewModel: MainViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
) {
  val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

  when (mainUiState) {
    is UIState.Loading -> {
      Skeleton()
    }

    is UIState.Ready -> {
      val data = mainUiState.data

      if (data == null || (!data.hasValidSession || data.user == null)) {
        LoadingIndicator(message = "Signing Out...", modifier = Modifier.fillMaxSize())
        LaunchedEffect(mainUiState) {
          onNavigateToUnauthenticated()
        }
        return
      }

      // ensured valid user exists at this point
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      TravelPlanScreen(
        uiState, data, modifier
      )
    }

    is UIState.Error -> {
      Text("Error")
    }

    else -> {
      Skeleton()
    }
  }
}

@Composable
fun TravelPlanScreen(
  uiState: TravelPlanUiState,
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    topBar = { FreyzaTpAppBar() },
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) {
    if (mainUiState.user == null || mainUiState.today == null) {
      return@Scaffold
    }

    LazyColumn(
      contentPadding = PaddingValues(
        vertical = dimensionResource(R.dimen.default_spacing).times(8),
        horizontal = dimensionResource(R.dimen.default_spacing).times(4)
      ),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
        .fillMaxSize()
        .padding(it)
    ) {

      item {
        when (val travelPlan = uiState.currentTravelPlan) {
          is UIState.Loading -> {
            LoadingIndicator(message = "Loading Travel Plan..")
          }

          is UIState.Ready -> {
            DebugTravelPlan(travelPlan.data)
          }

          is UIState.Error -> {
            Text(travelPlan.message.toString())
          }

          else -> {}
        }
      }
    }
  }
}


@Composable
private fun DebugTravelPlan(travelPlan: TravelPlan?, modifier: Modifier = Modifier) {
  Card {
    Column(modifier = modifier.padding(8.dp)) {

      if (travelPlan === null) {
        Text("No Travel Plan")
      } else {
        Text(travelPlan.id, fontFamily = FontFamily.Monospace)
        Text(travelPlan.employeeId)
        Text(DateFormatter.format(travelPlan.month))
        Text(travelPlan.createdById)

        Text(travelPlan.travelPlanEntries.size.toString())

        Text(DateFormatter.format(travelPlan.createdAt))
        travelPlan.updatedAt?.let {
          Text(DateFormatter.format(it))
        }
      }
    }
  }
}
