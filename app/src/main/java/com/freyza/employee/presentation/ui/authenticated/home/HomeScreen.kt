package com.freyza.employee.presentation.ui.authenticated.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.freyza.employee.common.MainViewModel
import com.freyza.employee.common.Result
import com.freyza.employee.domain.model.Expense
import com.freyza.employee.domain.model.HomeScreenUiState
import com.freyza.employee.domain.model.MainUiState
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyExpenses
import com.freyza.employee.domain.model.dummyUser
import com.freyza.employee.presentation.ui.composables.HomeScreenSkeleton
import com.freyza.employee.presentation.ui.composables.LoadingScreen
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime

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

    when (mainUiState) {
        is Result.Loading -> {
            HomeScreenSkeleton()
        }

        is Result.Success -> {
            HomeScreen(
                uiState,
                mainUiState.data!!,
                onNavigateToUnauthenticated,
                onLogoutClicked,
                modifier
            )
        }

        is Result.Error -> {
            Text("Error")
        }
    }

}

@Composable
fun HomeScreen(
    uiState: Result<HomeScreenUiState>,
    mainUiState: MainUiState,
    onNavigateToUnauthenticated: () -> Unit,
    onLogoutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!mainUiState.hasValidSession || mainUiState.user == null) {
            LoadingScreen(message = "Signing Out...")

            LaunchedEffect(Unit) {
                onNavigateToUnauthenticated()
            }
        }

        if (mainUiState.user == null) {
            return
        }

        Text("Welcome back ${mainUiState.user.name}")

        DebugUserInfo(mainUiState.user)

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is Result.Loading -> {
                LoadingScreen(message = "Loading your recent expenses...")
            }

            is Result.Success -> {
                ExpenseList(uiState.data!!.recentExpenses)
            }

            is Result.Error -> {
                Text(uiState.message.toString())
            }
        }


        Button(onClick = onLogoutClicked) { Text("Logout") }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun DebugUserInfo(user: User, modifier: Modifier = Modifier) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {

            Text(user.id, fontFamily = FontFamily.Monospace)
            Text(user.name)
            Text(user.role.titleCase())
            Text(user.status.titleCase())
            Text(user.location.toString())
            Text(
                user.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(LocalDateTime.Format {
                        day()
                        char('/')
                        monthName(MonthNames.ENGLISH_ABBREVIATED)
                        char('/')
                        year()
                    })
            )

            user.updatedAt?.let {
                Text(
                    it.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Format {
                            day()
                            char('/')
                            monthName(MonthNames.ENGLISH_ABBREVIATED)
                            char('/')
                            year()
                        })
                )
            }

            user.userInfo?.email?.let { Text(it) }
            user.userInfo?.lastSignInAt?.let {
                Text(
                    it.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Format {
                            day()
                            char('/')
                            monthName(MonthNames.ENGLISH_ABBREVIATED)
                            char('/')
                            year()
                        })
                )
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
        HomeScreen(
            Result.Success(HomeScreenUiState(dummyExpenses())),
            MainUiState(hasValidSession = true, user = dummyUser()),
            {},
            {})
    }
}
