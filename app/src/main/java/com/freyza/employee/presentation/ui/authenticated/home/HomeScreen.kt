package com.freyza.employee.presentation.ui.authenticated.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.common.Result
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
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
    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Text("Home Screen")
            Button(onClick = onLogoutClicked) { "Logout" }
        }

        when (val result = uiState) {
            is Result.Success -> {
                LaunchedEffect(Unit) { onNavigateToUnauthenticated() }
            }

            is Result.Error -> {
                Log.d("APP", "Error logging out")
                Text(text = "Error", color = Color.Red)
            }

            is Result.Loading -> CircularProgressIndicator()
            else -> {}
        }
    }
}
