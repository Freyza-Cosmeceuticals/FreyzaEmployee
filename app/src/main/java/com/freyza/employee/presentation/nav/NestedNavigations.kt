package com.freyza.employee.presentation.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.freyza.employee.presentation.ui.authenticated.addvisit.AddVisitScreenRoute
import com.freyza.employee.presentation.ui.authenticated.dailyreport.DailyReportScreenRoute
import com.freyza.employee.presentation.ui.authenticated.home.HomeScreenRoute
import com.freyza.employee.presentation.ui.authenticated.profile.ProfileScreenRoute
import com.freyza.employee.presentation.ui.authenticated.travelplan.TravelPlanScreenRoute
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.unauthenticated.login.LoginScreenRoute
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/*
* Builds the unauthenticated navigation graph
*/
fun NavGraphBuilder.unauthenticatedGraph(navController: NavController) {

  navigation<NavRoutes.Unauthenticated.NavigationRoute>(
    startDestination = NavRoutes.Unauthenticated.Login
  ) {

    composable<NavRoutes.Unauthenticated.Login> {
      val vm = koinViewModel<LoginViewModel>()
      LoginScreenRoute(
        viewModel = vm,
        // We will never navigate to registration, although
        onNavigateToRegistration = {
          navController.navigate(NavRoutes.Unauthenticated.Register)
        }, onNavigateToAuthenticatedRoute = {
          navController.navigate(NavRoutes.Authenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Unauthenticated.NavigationRoute) {
              inclusive = true
            }
          }
        })
    }

    composable<NavRoutes.Unauthenticated.Register> {
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
fun NavGraphBuilder.authenticatedGraph(navController: NavController, mainUiState: MainUiState) {

  navigation<NavRoutes.Authenticated.NavigationRoute>(
    startDestination = NavRoutes.Authenticated.Home
  ) {
    composable<NavRoutes.Authenticated.Home> {
      val vm = koinViewModel<HomeViewModel>()

      HomeScreenRoute(mainUiState = mainUiState, viewModel = vm, onNavigateToUnauthenticated = {
        navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
          popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
            inclusive = true
          }
        }
      })
    }

    composable<NavRoutes.Authenticated.TravelPlan> {
      val vm = koinViewModel<TravelPlanViewModel>()
      TravelPlanScreenRoute(
        mainUiState = mainUiState, viewModel = vm, onNavigateToUnauthenticated = {
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        })
    }

    composable<NavRoutes.Authenticated.DailyReports> {
      val vm = koinViewModel<DailyReportViewModel>()
      DailyReportScreenRoute(
        mainUiState = mainUiState,
        viewModel = vm,
        onNavigateToUnauthenticated = {
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        },
        onNavigateToAddVisit = { visitType ->
          navController.navigate(route = NavRoutes.Authenticated.AddVisit(type = visitType))
        })
    }

    composable<NavRoutes.Authenticated.AddVisit> { navBackStackEntry ->
      val visitType = navBackStackEntry.toRoute<NavRoutes.Authenticated.AddVisit>().type
      val vm = koinViewModel<AddVisitViewModel>(
        parameters = { parametersOf(visitType) })

      AddVisitScreenRoute(mainUiState = mainUiState, viewModel = vm, onNavigateUp = {
        navController.popBackStack()
      }, onNavigateToUnauthenticated = {
        navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
          popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
            inclusive = true
          }
        }
      })
    }

    composable<NavRoutes.Authenticated.Profile> {
      val vm = koinViewModel<ProfileViewModel>()
      ProfileScreenRoute(
        mainUiState = mainUiState, viewModel = vm, onNavigateToUnauthenticated = {
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        })
    }

//    composable<NavRoutes.Authenticated.ExpenseDetail> { navBackStackEntry ->
//      val expenseId = navBackStackEntry.toRoute<NavRoutes.Authenticated.ExpenseDetail>().expenseId
//      Text("Expense detail Route for $expenseId")
//    }
  }
}
