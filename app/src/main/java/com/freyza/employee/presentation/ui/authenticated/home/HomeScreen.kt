package com.freyza.employee.presentation.ui.authenticated.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.common.Result
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.util.Logger
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToUnauthenticated: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val onLogoutClicked = { viewModel.logout() }

    HomeScreen(uiState, onNavigateToUnauthenticated, onLogoutClicked, modifier)
}

@Composable
fun HomeScreen(
    uiState: Result<Nothing>?,
    onNavigateToUnauthenticated: () -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen")
        Button(onClick = onLogoutClicked) { Text("Logout") }

        when (val result = uiState) {
            is Result.Success -> {
                LaunchedEffect(Unit) { onNavigateToUnauthenticated() }
            }

            is Result.Error -> {
                Logger.d("APP", "Error logging out")
                Text(text = "Error", color = Color.Red)
            }

            is Result.Loading -> CircularProgressIndicator()
            else -> {}
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    FreyzaEmployeeTheme {
        HomeScreen(null, {}, {})
    }
}
