package com.freyza.employee.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freyza.employee.data.models.ExpenseEntry
import com.freyza.employee.data.repository.FakeExpenseRepository
import com.freyza.employee.util.Result

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel(), modifier: Modifier = Modifier) {
    val itemsResult by viewModel.itemsLiveData.observeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard")
        Spacer(Modifier.height(24.dp))
        Button(onClick = {}) {
            Text("Logout")
        }

        when (itemsResult) {
            is Result.Success -> {
                LazyColumn() {
                    items((itemsResult as Result.Success<List<ExpenseEntry>>).data) {
                        Text(it.id)
                    }
                }
            }

            is Result.Error -> {
                Text("Unable to fetch expenses data ${(itemsResult as Result.Error).exception.toString()}")
            }

            else -> {}
        }

    }
}

@Preview(showSystemUi = true, showBackground = true)
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(DashboardViewModel(FakeExpenseRepository()))
}
