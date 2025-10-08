package com.freyza.employee

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    if (uiState.isLoading) {
        LoadingScreen()
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            unauthenticatedGraph(navController = navController)

            authenticatedGraph(navController = navController)
        }
    }
}


@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
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
