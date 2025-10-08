package com.freyza.employee

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.common.MainViewModel
import com.freyza.employee.presentation.nav.NavigationRoutes
import com.freyza.employee.presentation.nav.authenticatedGraph
import com.freyza.employee.presentation.nav.unauthenticatedGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun FreyzaEmployeeApp(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    ToastDebug(mainViewModel = mainViewModel)

    val startDestination =
        if (uiState.hasValidSession) NavigationRoutes.Authenticated.NavigationRoute else NavigationRoutes.Unauthenticated.NavigationRoute

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        unauthenticatedGraph(navController = navController)

        authenticatedGraph(navController = navController)
    }
}

@Composable
fun ToastDebug(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        mainViewModel.toastMessageFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
