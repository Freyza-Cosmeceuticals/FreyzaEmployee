package com.freyza.employee.presentation.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.authenticated.addvisit.AddVisitScreenRoute
import com.freyza.employee.presentation.ui.authenticated.dailyreport.DailyReportScreenRoute
import com.freyza.employee.presentation.ui.authenticated.home.HomeScreenRoute
import com.freyza.employee.presentation.ui.authenticated.profile.ProfileScreenRoute
import com.freyza.employee.presentation.ui.authenticated.reportdetail.ReportDetailScreenRoute
import com.freyza.employee.presentation.ui.authenticated.travelplan.TravelPlanScreenRoute
import com.freyza.employee.presentation.ui.authenticated.visitdetail.VisitDetailScreenRoute
import com.freyza.employee.presentation.ui.unauthenticated.login.LoginScreenRoute
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import com.freyza.employee.presentation.ui.viewmodels.ReportDetailViewModel
import com.freyza.employee.presentation.ui.viewmodels.VisitDetailViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

const val TAG = "NavigationGraph"

/*
* Builds the unauthenticated navigation graph
*/
fun NavGraphBuilder.unauthenticatedGraph(navController: NavController) {
  navigation<NavRoutes.Unauthenticated.NavigationRoute>(
    startDestination = NavRoutes.Unauthenticated.Login
  ) {

    composable<NavRoutes.Unauthenticated.Login> {
      LoginScreenRoute(
        onNavigateToAuthenticatedRoute = {
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
fun NavGraphBuilder.authenticatedGraph(
  navController: NavController,
  onExit: () -> Unit,
  onLogout: () -> Unit,
) {
  navigation<NavRoutes.Authenticated.NavigationRoute>(
    startDestination = NavRoutes.Authenticated.Home
  ) {
    composable<NavRoutes.Authenticated.Home> {
      HomeScreenRoute(
        onNavigateToUnauthenticated = {
          onLogout()
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        },
        onNavigateToReport = { reportId: String ->
          navController.navigate(
            route = NavRoutes.Authenticated.ReportDetail(
              reportId = reportId
            )
          )
        },
        onNavigateToAddVisit = { visitType: VisitType, reportId: String, employeeId: String ->
          navController.navigate(
            route = NavRoutes.Authenticated.AddVisit(
              type = visitType, reportId = reportId, employeeId = employeeId
            )
          )
        },
        onExit = onExit,
      )
    }

    composable<NavRoutes.Authenticated.TravelPlan> {
      TravelPlanScreenRoute(
        onNavigateToUnauthenticated = {
          onLogout()
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        })
    }

    composable<NavRoutes.Authenticated.DailyReports> { navBackStackEntry ->
      val visitCreated by navBackStackEntry.savedStateHandle.getStateFlow<Boolean?>("created", null)
        .collectAsStateWithLifecycle()

      LaunchedEffect(visitCreated) {
        if (visitCreated != null) {
          Logger.d(
            TAG, "Got `created` from AddVisit's backstack entry: ${visitCreated.toString()}"
          )
        }
      }

      DailyReportScreenRoute(visitCreated = visitCreated, onVisitCreatedConsumed = {
        // The child screen calls this AFTER it has shown the UI change
        navBackStackEntry.savedStateHandle["created"] = null
      }, onNavigateToReportDetail = { reportId ->
        navController.navigate(route = NavRoutes.Authenticated.ReportDetail(reportId = reportId))

      }, onNavigateToAddVisit = { visitType, reportId, employeeId ->
        navController.navigate(
          route = NavRoutes.Authenticated.AddVisit(
            type = visitType, reportId = reportId, employeeId = employeeId
          )
        )
      })
    }

    composable<NavRoutes.Authenticated.ReportDetail> { navBackStackEntry ->
      val reportId = navBackStackEntry.toRoute<NavRoutes.Authenticated.ReportDetail>().reportId

      val visitCreated by navBackStackEntry.savedStateHandle.getStateFlow<Boolean?>("created", null)
        .collectAsStateWithLifecycle()

      val visitDeleted by navBackStackEntry.savedStateHandle.getStateFlow<Boolean?>("deleted", null)
        .collectAsStateWithLifecycle()

      LaunchedEffect(visitCreated, visitDeleted) {
        visitCreated?.let {
          Logger.d(
            TAG, "Got `created` from AddVisit's backstack entry: $it"
          )
        }
        visitDeleted?.let {
          Logger.d(
            TAG, "Got `deleted` from VisitDetail's backstack entry: $it"
          )
        }
      }

      val vm = koinViewModel<ReportDetailViewModel>(
        parameters = { parametersOf(reportId) })

      ReportDetailScreenRoute(
        viewModel = vm,
        visitCreated = visitCreated,
        onVisitCreatedConsumed = {
          // The child screen calls this AFTER it has shown the UI change
          navBackStackEntry.savedStateHandle["created"] = null
        },
        visitDeleted = visitDeleted,
        onVisitDeletedConsumed = {
          navBackStackEntry.savedStateHandle["deleted"] = null
        },
        onNavigateUp = {
          navController.popBackStack()
        },
        onNavigateToVisitDetail = { visitId ->
          navController.navigate(route = NavRoutes.Authenticated.VisitDetail(visitId = visitId))
        },
        onNavigateToAddVisit = { visitType, reportId, employeeId ->
          navController.navigate(
            route = NavRoutes.Authenticated.AddVisit(
              type = visitType, reportId = reportId, employeeId = employeeId
            )
          )
        })
    }

    composable<NavRoutes.Authenticated.VisitDetail> { navBackStackEntry ->
      val visitId = navBackStackEntry.toRoute<NavRoutes.Authenticated.VisitDetail>().visitId

      val visitUpdated by navBackStackEntry.savedStateHandle.getStateFlow<Boolean?>("updated", null)
        .collectAsStateWithLifecycle()

      LaunchedEffect(visitUpdated) {
        visitUpdated?.let {
          Logger.d(
            TAG, "Got `updated` from AddVisit's backstack entry: $it"
          )
        }
      }

      val vm = koinViewModel<VisitDetailViewModel>(
        parameters = { parametersOf(visitId) })

      VisitDetailScreenRoute(viewModel = vm, visitUpdated = visitUpdated, onVisitUpdatedConsumed = {
        // The child screen calls this AFTER it has shown the UI change
        navBackStackEntry.savedStateHandle["updated"] = null
      }, onNavigateUp = { deleted ->
        Logger.d(
          TAG, "Navigating back from VisitDetail, deleted: ${deleted}, saving to savestate"
        )

        navController.previousBackStackEntry?.savedStateHandle?.set("deleted", deleted)
        navController.popBackStack()
      }, onEditVisit = { visit ->
        navController.navigate(
          NavRoutes.Authenticated.AddVisit(
            type = visit.visitType,
            reportId = visit.reportId,
            employeeId = visit.employeeId,
            visitId = visit.id
          )
        )
      })
    }

    composable<NavRoutes.Authenticated.AddVisit> { navBackStackEntry ->
      val visitType = navBackStackEntry.toRoute<NavRoutes.Authenticated.AddVisit>().type
      val reportId = navBackStackEntry.toRoute<NavRoutes.Authenticated.AddVisit>().reportId
      val employeeId = navBackStackEntry.toRoute<NavRoutes.Authenticated.AddVisit>().employeeId
      val visitId = navBackStackEntry.toRoute<NavRoutes.Authenticated.AddVisit>().visitId

      val vm = koinViewModel<AddVisitViewModel>(
        parameters = { parametersOf(visitType, reportId, employeeId, visitId) })

      AddVisitScreenRoute(viewModel = vm, onNavigateUp = { created, updated ->
        Logger.d(
          TAG,
          "Navigating back from AddVisit, created: ${created}, updated: ${updated}, saving to previous backstack's savestate"
        )

        navController.previousBackStackEntry?.savedStateHandle?.set("created", created)
        navController.previousBackStackEntry?.savedStateHandle?.set("updated", updated)
        navController.popBackStack()
      }, onNavigateToUnauthenticated = {
        onLogout()
        navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
          popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
            inclusive = true
          }
        }
      })
    }

    composable<NavRoutes.Authenticated.Profile> {
      ProfileScreenRoute(
        onNavigateToUnauthenticated = {
          onLogout()
          navController.navigate(route = NavRoutes.Unauthenticated.NavigationRoute) {
            popUpTo(route = NavRoutes.Authenticated.NavigationRoute) {
              inclusive = true
            }
          }
        })
    }
  }
}


fun NavController.navigateToTab(route: NavRoutes) {
  navigate(route) {
    popUpTo(NavRoutes.Authenticated.Home) {
      inclusive = false
      saveState = true
    }
    launchSingleTop = true
    restoreState = true
  }
}
