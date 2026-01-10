package com.freyza.employee.presentation.ui.authenticated.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyExpenses
import com.freyza.employee.domain.model.dummyTravelPlan
import com.freyza.employee.domain.model.dummyTravelPlanEntry
import com.freyza.employee.domain.model.dummyUser
import com.freyza.employee.presentation.ui.authenticated.home.composables.HomeScreenSkeleton
import com.freyza.employee.presentation.ui.composables.LoadingIndicator
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.MainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateToUnauthenticated: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    val onLogoutClicked = { mainViewModel.logout() }
    val loadTravelPlan: () -> Unit = { viewModel.loadCurrentTravelPlan() }

    when (mainUiState) {
        is UIState.Loading -> {
            HomeScreenSkeleton()
        }

        is UIState.Ready -> {
            ActualHomeScreen(
                uiState,
                mainUiState.data!!,
                onNavigateToUnauthenticated,
                onLogoutClicked,
                loadTravelPlan,
                modifier
            )
        }

        is UIState.Error -> {
            Text("Error")
        }

        else -> {}
    }

}

@Composable
private fun ActualHomeScreen(
    uiState: HomeScreenUiState,
    mainUiState: MainUiState,
    onNavigateToUnauthenticated: () -> Unit,
    onLogoutClicked: () -> Unit,
    loadTravelPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (!mainUiState.hasValidSession || mainUiState.user == null) {
            LoadingIndicator(message = "Signing Out...")

            LaunchedEffect(Unit) {
                onNavigateToUnauthenticated()
            }
        }

        if (mainUiState.user == null) {
            return
        }

        LaunchedEffect(mainUiState.user.id) {
            loadTravelPlan()
        }

        Text("Welcome back ${mainUiState.user.name}")
        DebugUserInfo(mainUiState.user)
        Spacer(modifier = Modifier.height(16.dp))

        // ExpenseList(uiState.data!!.recentExpenses)

        when (val travelPlan = uiState.currentTravelPlan) {
            is UIState.Loading -> {
                LoadingIndicator(message = "Loading Travel Plan..")
            }

            is UIState.Ready -> {
                DebugTravelPlan(travelPlan.data)
            }

            is UIState.Error -> {
                Text(travelPlan.message.toString())
            }

            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val travelPlanEntry = uiState.todayTravelPlanEntry) {
            is UIState.Loading -> {
                LoadingIndicator(message = "Loading Travel Plan Entry...")
            }

            is UIState.Ready -> {
                DebugTravelPlanEntry(travelPlanEntry.data)
            }

            is UIState.Error -> {
                Text(travelPlanEntry.message.toString())
            }

            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogoutClicked) { Text("Logout") }
    }
}


@Composable
private fun DebugUserInfo(user: User, modifier: Modifier = Modifier) {
    Card {
        Column(modifier = modifier.padding(8.dp)) {

            Text(user.id, fontFamily = FontFamily.Monospace)
            Text(user.name)
            Text(user.email)
            Text(user.phone)

            Text(user.role.titleCase())
            Text(user.status.titleCase())

            Text(user.tier?.fullForm.toString())
            Text(user.hqId.toString())

            Text(DateFormatter.format(user.createdAt))
            user.updatedAt?.let {
                Text(DateFormatter.format(it))
            }

            user.userInfo?.lastSignInAt?.let {
                Text(DateFormatter.format(it))
            }
        }
    }
}

@Composable
private fun DebugTravelPlan(travelPlan: TravelPlan?, modifier: Modifier = Modifier) {
    Card {
        Column(modifier = modifier.padding(8.dp)) {

            if (travelPlan === null) {
                Text("No Travel Plan")
            } else {
                Text(travelPlan.id, fontFamily = FontFamily.Monospace)
                Text(travelPlan.employeeId)
                Text(DateFormatter.format(travelPlan.month))
                Text(travelPlan.createdById)

                Text(travelPlan.travelPlanEntries.size.toString())

                Text(DateFormatter.format(travelPlan.createdAt))
                travelPlan.updatedAt?.let {
                    Text(DateFormatter.format(it))
                }
            }
        }
    }
}

@Composable
private fun DebugTravelPlanEntry(travelPlanEntry: TravelPlanEntry?, modifier: Modifier = Modifier) {
    Card {
        Column(modifier = modifier.padding(8.dp)) {

            if (travelPlanEntry === null) {
                Text("No Travel Plan Entry")
            } else {
                Text(travelPlanEntry.id, fontFamily = FontFamily.Monospace)
                Text(DateFormatter.format(travelPlanEntry.date))
                Text(travelPlanEntry.dayType.titleCase())
                Text(travelPlanEntry.routeId.toString())

                Text(DateFormatter.format(travelPlanEntry.createdAt))
                travelPlanEntry.updatedAt?.let {
                    Text(DateFormatter.format(it))
                }
            }
        }
    }
}


@Composable
fun ExpenseList(expenses: List<Expense>) {

    if (expenses.isEmpty()) {
        Text("No Recent Expenses Found")
    }

    LazyColumn {
        items(expenses) {
            Card(modifier = Modifier.padding(8.dp)) {
                Text(it.id)
                Text(it.location)
                Text("${it.distance}km")
                Text("$${it.cost}")
                Text(if (it.locked) "Locked" else "Open")
            }
        }


    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun HomeScreenPreview() {
    FreyzaEmployeeTheme {
        ActualHomeScreen(
            HomeScreenUiState(
            UIState.Ready(dummyExpenses()),
            currentTravelPlan = UIState.Ready(dummyTravelPlan()),
            todayTravelPlanEntry = UIState.Ready(dummyTravelPlanEntry())
        ), MainUiState(hasValidSession = true, user = dummyUser()), {}, {}, {})
    }
}
