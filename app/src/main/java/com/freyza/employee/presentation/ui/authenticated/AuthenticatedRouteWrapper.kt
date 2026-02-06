package com.freyza.employee.presentation.ui.authenticated

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.state.MainUiState
import kotlinx.coroutines.delay

@Composable
fun AuthenticatedRouteWrapper(
  mainUiState: MainUiState,
  onNavigateToUnauthenticated: () -> Unit,
  loading: @Composable () -> Unit,
  timeoutMillis: Long = 5_000,
  content: @Composable (MainUiState) -> Unit,
) {
  var timedOut by remember { mutableStateOf(false) }

  LaunchedEffect(mainUiState) {
    timedOut = false
    if (!mainUiState.hasValidSession || mainUiState.user == null) {
      delay(timeoutMillis)
      timedOut = true
    }
  }

  when {
    mainUiState.hasValidSession && mainUiState.user != null -> {
      content(mainUiState)
    }

    !timedOut -> {
      loading()
    }

    else -> {
      LoadingIndicator(message = "Signing Out...", modifier = Modifier.fillMaxSize())
      LaunchedEffect(Unit) {
        onNavigateToUnauthenticated()
      }
    }
  }
}
