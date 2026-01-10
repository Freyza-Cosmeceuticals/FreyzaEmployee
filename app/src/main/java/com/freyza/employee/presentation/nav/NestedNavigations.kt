package com.freyza.employee.presentation.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.freyza.employee.presentation.ui.authenticated.home.HomeScreen
import com.freyza.employee.presentation.ui.unauthenticated.login.LoginScreen

/*
* Builds the unauthenticated navigation graph
*/
fun NavGraphBuilder.unauthenticatedGraph(navController: NavController) {

    navigation<NavigationRoutes.Unauthenticated.NavigationRoute>(
        startDestination = NavigationRoutes.Unauthenticated.Login
    ) {

        composable<NavigationRoutes.Unauthenticated.Login> {
            LoginScreen(
                // We will never navigate to registration, although
                onNavigateToRegistration = {
                    navController.navigate(NavigationRoutes.Unauthenticated.Register)
                },
                onNavigateToAuthenticatedRoute = {
                    navController.navigate(NavigationRoutes.Authenticated.NavigationRoute) {
                        popUpTo(route = NavigationRoutes.Unauthenticated.NavigationRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<NavigationRoutes.Unauthenticated.Register> {
            Column {

                Text("Register Screen is not available on Employee App")
                Button(onClick = { navController.navigateUp() }) { Text("Back") }
            }
        }
    }


}

/*
* Builds the Authenticated Navigation Graph
 */
fun NavGraphBuilder.authenticatedGraph(navController: NavController) {

    navigation<NavigationRoutes.Authenticated.NavigationRoute>(
        startDestination = NavigationRoutes.Authenticated.Home
    ) {

        composable<NavigationRoutes.Authenticated.Home> {
            HomeScreen(onNavigateToUnauthenticated = {
                navController.navigate(route = NavigationRoutes.Unauthenticated.NavigationRoute) {
                    popUpTo(route = NavigationRoutes.Authenticated.NavigationRoute) {
                        inclusive = true
                    }
                }
            })
        }

        composable<NavigationRoutes.Authenticated.TravelPlan> {
            Text("Current Travel Plan Here")
        }

        composable<NavigationRoutes.Authenticated.ExpenseHistory> {
            Text("Expense History Here")
        }

        composable<NavigationRoutes.Authenticated.AddExpense> {
            Text("Add Expense Route")
        }

        composable<NavigationRoutes.Authenticated.ExpenseDetail> { navBackStackEntry ->
            val expenseId =
                navBackStackEntry.toRoute<NavigationRoutes.Authenticated.ExpenseDetail>().expenseId
            Text("Expense detail Route for $expenseId")
        }
    }
}
