package com.freyza.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.core.network.NetworkMonitor
import com.freyza.employee.core.showTypedSnackbar
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.model.AuthState
import com.freyza.employee.presentation.nav.NavRoutes
import com.freyza.employee.presentation.nav.authenticatedGraph
import com.freyza.employee.presentation.nav.unauthenticatedGraph
import com.freyza.employee.presentation.ui.AppUpdateGateway
import com.freyza.employee.presentation.ui.GPSGateway
import com.freyza.employee.presentation.ui.composables.FreyzaBottomNavBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.composables.LocalSnackbarHostState
import com.freyza.employee.presentation.ui.composables.OfflineBanner
import com.freyza.employee.presentation.ui.composables.VersionInfo
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.AppUpdateViewModel
import com.freyza.employee.presentation.ui.viewmodels.SessionViewModel
import io.sentry.Sentry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun FreyzaEmployeeApp(
  navController: NavHostController = rememberNavController(),
  sessionViewModel: SessionViewModel = koinActivityViewModel(),
  appUpdateViewModel: AppUpdateViewModel = koinActivityViewModel(),
  snackbarManager: SnackbarManager = koinInject(),
  networkMonitor: NetworkMonitor = koinInject(),
) {
  val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
  val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  // load auth and setup snackbar consumer
  LaunchedEffect(Unit) {
    sessionViewModel.checkAuth()
    sessionViewModel.syncTime()

    snackbarManager.messages.collect { message ->
      val result = snackbarHostState.showTypedSnackbar(
        message = message.message,
        type = message.type,
        actionLabel = message.actionLabel,
        duration = message.duration,
        withDismissAction = message.withDismissAction
      )
      if (result == SnackbarResult.ActionPerformed) {
        message.onAction?.invoke()
      }
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    if (authState is AuthState.Authenticated) {
      appUpdateViewModel.checkForUpdate()
      sessionViewModel.syncTime()
    }
  }

  LaunchedEffect(authState) {
    if (authState is AuthState.Authenticated) {
      appUpdateViewModel.checkForUpdate()
      sessionViewModel.syncTime()
    }
  }

  CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        OfflineBanner(
          isOnline = isOnline,
          onRefresh = {
            sessionViewModel.checkAuth()
            sessionViewModel.syncTime()
          })

        AppUpdateGateway(viewModel = appUpdateViewModel) {
          Box(
            modifier = Modifier.weight(1f)
              // If the device is offline (banner visible), consume the top insets
              // so the Scaffold inside this Box doesn't double-pad the TopAppBar.
              .let {
                if (!isOnline) it.consumeWindowInsets(WindowInsets.statusBars) else it
              }) {
            when (val state = authState) {
              is AuthState.Loading -> {
                Scaffold {
                  LoadingIndicator(
                    message = "Loading, please wait...",
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(it),
                  )
                }
              }

              is AuthState.Error -> {
                Scaffold {
                  FreyzaEmployeeAppError(
                    message = state.message,
                    date = sessionViewModel.getTodayDateFormatted(),
                    onRetry = sessionViewModel::checkAuth,
                    onLogout = sessionViewModel::logout,
                    modifier = Modifier.padding(it)
                  )
                }
              }

              else -> {
                GPSGateway {
                  Scaffold(
                    // outer scaffold only pads system status and nav bars, not keyboards
                    // children scaffold or their children should apply their scaffold's paddingValues
                    // and are responsible any ime paddings
                    //
                    // they don't need to handle any system bar padding
                    contentWindowInsets = WindowInsets.systemBars, bottomBar = {
                      if (state is AuthState.Authenticated) {
                        FreyzaBottomNavBar(navController)
                      }
                    }) { paddingValues ->
                    Surface(
                      modifier = Modifier
                        // outer scaffold only handles bottom padding (bar + inner fabs)
                        // don't apply padding for top bars
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        // consume exactly the bottom bar needs, to prevent double apply
                        // top handled by TopAppBars, to fill in the status bar
                        .consumeWindowInsets(WindowInsets(bottom = paddingValues.calculateBottomPadding()))
                    ) {
                      val startDestination = if (state is AuthState.Authenticated) {
                        NavRoutes.Authenticated.NavigationRoute
                      } else {
                        NavRoutes.Unauthenticated.NavigationRoute
                      }

                      DisposableEffect(navController) {
                        val listener =
                          NavController.OnDestinationChangedListener { _, destination, _ ->
                            Logger.d(
                              "AppNavController",
                              "Destination changed: ${destination.route}"
                            )

                            if (!BuildConfig.DEBUG) {
                              Sentry.setTag("current_screen", destination.route)
                            }
                          }

                        navController.addOnDestinationChangedListener(listener)

                        onDispose {
                          navController.removeOnDestinationChangedListener(listener)
                        }
                      }

                      NavHost(
                        navController = navController, startDestination = startDestination
                      ) {
                        unauthenticatedGraph(navController = navController)
                        authenticatedGraph(
                          navController = navController,
                          onExit = { sessionViewModel.exit(context) },
                          onLogout = sessionViewModel::logout
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // TODO: Move this up to the navbarHost property of Scaffolds instead, for better placement
      Box(
        modifier = Modifier
          .fillMaxSize()
          .zIndex(10f), contentAlignment = Alignment.BottomCenter
      ) {
        FreyzaSnackbarHost(
          hostState = snackbarHostState, modifier = Modifier.padding(bottom = 84.dp)
        )
      }
    }
  }
}

@Composable
private fun FreyzaEmployeeAppError(
  message: String,
  date: String,
  onRetry: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(dimensionResource(R.dimen.screen_padding)),
    verticalArrangement = Arrangement.spacedBy(
      dimensionResource(R.dimen.default_spacing).times(2), Alignment.CenterVertically
    ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Icon(
      painter = painterResource(R.drawable.error_24px),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.error,
      modifier = Modifier.size(64.dp)
    )

    Text(
      text = message,
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

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ErrorPreview() {
  FreyzaEmployeeTheme {
    FreyzaEmployeeAppError(
      "Random Error",
      date = DateFormatter.format(
        LocalDate(
          year = 2025, month = Month.DECEMBER, day = 25
        )
      ),
      onRetry = {}, onLogout = {},
    )
  }
}
