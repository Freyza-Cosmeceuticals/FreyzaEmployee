package com.freyza.employee

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.core.SnackbarType
import com.freyza.employee.core.UIState
import com.freyza.employee.core.showTypedSnackbar
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.presentation.nav.NavRoutes
import com.freyza.employee.presentation.nav.authenticatedGraph
import com.freyza.employee.presentation.nav.unauthenticatedGraph
import com.freyza.employee.presentation.ui.composables.FreyzaBottomNavBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.VersionInfo
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.androidx.compose.koinViewModel

@Composable
fun FreyzaEmployeeApp(
  navController: NavHostController = rememberNavController(),
  mainViewModel: MainViewModel = koinViewModel(),
) {
  val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
  ToastDebug(mainViewModel = mainViewModel)

  when (val res = uiState) {
    is UIState.Loading -> {
      Scaffold { paddingValues ->
        LoadingIndicator(
          Modifier
            .fillMaxSize()
            .padding(paddingValues)
        )
      }
    }

    is UIState.Ready -> {
      // if valid login found, start with the Authenticated route, otherwise the Unauthenticated route.
      val startDestination =
        if (res.data?.hasValidSession == true && res.data.user != null) NavRoutes.Authenticated.NavigationRoute else NavRoutes.Unauthenticated.NavigationRoute

      Scaffold(
        bottomBar = { FreyzaBottomNavBar(navController) },
        contentWindowInsets = NavigationBarDefaults.windowInsets
      ) {
        Surface(modifier = Modifier.padding(it)) {
          NavHost(
            navController = navController, startDestination = startDestination
          ) {
            unauthenticatedGraph(navController = navController)
            authenticatedGraph(navController = navController, mainUiState = res.data!!)
          }
        }
      }
    }

    is UIState.Error -> {
      FreyzaEmployeeAppError(
        res.message,
        date = res.data?.today?.let { DateFormatter.format(it) } ?: "???",
        onRetry = { mainViewModel.initializeSession() },
        onLogout = { mainViewModel.logout() },
      )
    }

    else -> {}
  }
}

@Composable
private fun FreyzaEmployeeAppError(
  message: String?,
  date: String,
  onRetry: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val unknownErrorString = stringResource(R.string.error_unknown)

  Scaffold(
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) }, modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(dimensionResource(R.dimen.screen_padding)),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(4), Alignment.CenterVertically
      ),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LaunchedEffect(Unit) {
        scope.launch {
          snackbarHostState.showTypedSnackbar(
            message ?: unknownErrorString, type = SnackbarType.ERROR, withDismissAction = true
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))
      Icon(
        painter = painterResource(R.drawable.error_24px),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(64.dp)
      )

      Text(
        text = message ?: unknownErrorString,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

      Row(
        horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(3)
        )
      ) {
        Button(onClick = onRetry, modifier = Modifier.weight(1f)) {
          Text("Retry")
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f)) {
          Text("Logout")
        }
      }

      Spacer(modifier = Modifier.weight(0.75f))
      Text(
        date,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center
      )
      VersionInfo()
      Text(
        "Freyza Cosmeceuticals\nApp by Harsh Narayan Jha",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.weight(0.25f))
    }
  }
}

/*
* Authentication Debug Toasts
*/
@Composable
fun ToastDebug(mainViewModel: MainViewModel) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    mainViewModel.toastMessageFlow.collect { message ->
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }
}

@Preview
@Composable
private fun ErrorPreview() {
  FreyzaEmployeeTheme {
    FreyzaEmployeeAppError(
      "Random Error",
      date = DateFormatter.format(
        LocalDate(
          year = 2025,
          month = Month.DECEMBER,
          day = 25
        )
      ),
      onRetry = {},
      onLogout = {})
  }
}
