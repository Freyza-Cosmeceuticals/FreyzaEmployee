package com.freyza.employee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.data.repository.FakeExpenseRepository
import com.freyza.employee.data.repository.FakeUserRepository
import com.freyza.employee.ui.dashboard.DashboardScreen
import com.freyza.employee.ui.dashboard.DashboardViewModel
import com.freyza.employee.ui.login.LoginScreen
import com.freyza.employee.ui.login.LoginViewModel

@Composable
fun FreyzaEmployeeApp(navController: NavHostController = rememberNavController()) {
    val userRepository = FakeUserRepository()
    val dashboardRepository = FakeExpenseRepository()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val loginViewModel = remember { LoginViewModel(userRepository) }
            LoginScreen(viewModel = loginViewModel, onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            val dashboardViewModel = remember { DashboardViewModel(dashboardRepository) }
            DashboardScreen(viewModel = dashboardViewModel)
        }
    }
}
