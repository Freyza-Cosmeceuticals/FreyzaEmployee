package com.freyza.employee.presentation.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.common.Result
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    loginViewModel: LoginViewModel = koinViewModel(),
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier
) {
    val itemsResult by viewModel.itemsData.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { loginViewModel.logout(coroutineScope) }) {
            Text("Logout")
        }

        when (val result = itemsResult) {
            is Result.Success -> {
                LazyColumn() {
                    if (result.data.isNotEmpty()) {
                        items(result.data) {
                            Text(it.id)
                        }

                    } else {
                        item {
                            Text("No Expenses Found")
                        }
                    }
                }
            }

            is Result.Error -> {
                Text("Unable to fetch expenses data ${result.exception.toString()}")
            }

            is Result.Loading -> {
                CircularProgressIndicator()
            }
        }

    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
