package com.freyza.employee.presentation.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.freyza.employee.presentation.ui.authenticated.home.HomeScreen
import com.freyza.employee.presentation.ui.unauthenticated.login.LoginScreen

fun NavGraphBuilder.unauthenticatedGraph(navController: NavController) {

    navigation(
        route = NavigationRoutes.Unauthenticated.NavigationRoute.route,
        startDestination = NavigationRoutes.Unauthenticated.Login.route
    ) {

        composable(NavigationRoutes.Unauthenticated.Login.route) {
            LoginScreen(
                onNavigateToRegistration = {
                    navController.navigate(route = NavigationRoutes.Unauthenticated.Register.route)
                },
                onNavigateToAuthenticatedRoute = {
                    navController.navigate(route = NavigationRoutes.Authenticated.NavigationRoute.route) {
                        popUpTo(route = NavigationRoutes.Unauthenticated.NavigationRoute.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavigationRoutes.Unauthenticated.Register.route) {
            Column {

                Text("Register Screen is not available")
                Button(onClick = { navController.navigateUp() }) { Text("Back") }
            }
        }
    }


}

fun NavGraphBuilder.authenticatedGraph(navController: NavController) {

    navigation(
        route = NavigationRoutes.Authenticated.NavigationRoute.route,
        startDestination = NavigationRoutes.Authenticated.Home.route
    ) {

        composable(NavigationRoutes.Authenticated.Home.route) {
            HomeScreen(onNavigateToUnauthenticated = {
                navController.navigate(route = NavigationRoutes.Unauthenticated.NavigationRoute.route) {
                    popUpTo(route = NavigationRoutes.Authenticated.NavigationRoute.route) {
                        inclusive = true
                    }
                }
            })
        }

        composable(NavigationRoutes.Authenticated.AddExpense.route) {
            Text("Add Expense Route")
        }

        composable(
            NavigationRoutes.Authenticated.ExpenseDetail.route,
            arguments = ExpenseDetailDestination.arguments
        ) { navBackStackEntry ->
            val expenseId =
                navBackStackEntry.arguments?.getString(ExpenseDetailDestination.EXPENSE_ID)
            Text("Expense detail Route for $expenseId")
        }
    }
}
