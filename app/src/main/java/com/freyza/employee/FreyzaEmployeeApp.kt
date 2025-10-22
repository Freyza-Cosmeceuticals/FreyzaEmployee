package com.freyza.employee

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.common.MainViewModel
import com.freyza.employee.common.Result
import com.freyza.employee.presentation.nav.NavigationRoutes
import com.freyza.employee.presentation.nav.authenticatedGraph
import com.freyza.employee.presentation.nav.unauthenticatedGraph
import com.freyza.employee.presentation.ui.composables.FreyzaAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaBottomNavBar
import com.freyza.employee.presentation.ui.composables.LoadingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun FreyzaEmployeeApp(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    ToastDebug(mainViewModel = mainViewModel)

    // Change UI based on initial loading state
    when (val res = uiState) {
        is Result.Loading -> {
            Scaffold { paddingValues ->
                LoadingScreen(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }

        is Result.Success -> {
            // if valid login found, start with the Authenticated route, otherwise the UnAuthenticated route.
            val startDestination =
                if (res.data?.hasValidSession == true && res.data.user != null) NavigationRoutes.Authenticated.NavigationRoute else NavigationRoutes.Unauthenticated.NavigationRoute

            Scaffold(
                topBar = { FreyzaAppBar() },
                bottomBar = { FreyzaBottomNavBar(navController) }) { paddingValues ->
                Surface(modifier = Modifier.padding(paddingValues)) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        unauthenticatedGraph(navController = navController)
                        authenticatedGraph(navController = navController)
                    }
                }
            }
        }

        is Result.Error -> {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("An Unexpected Error has Occurred!")
                    Text(res.message.toString())
                    Button(onClick = { mainViewModel.initializeSession() }) { Text("Retry") }
                    Button(onClick = { mainViewModel.logout() }) { Text("Logout") }
                }
            }
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
