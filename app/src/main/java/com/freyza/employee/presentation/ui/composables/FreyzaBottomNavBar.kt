package com.freyza.employee.presentation.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freyza.employee.core.util.Logger
import com.freyza.employee.presentation.nav.BottomNavItem
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

private const val TAG = "BOTTOM_BAR"
private const val ROUTE_PREFIX = "com.freyza.employee.presentation.nav."

@Composable
fun FreyzaBottomNavBar(navController: NavController, modifier: Modifier = Modifier) {
    val screens = setOf(BottomNavItem.Home, BottomNavItem.TravelPlan, BottomNavItem.ExpenseHistory)

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Logger.d(
        TAG,
        "Current Screen Changed: ${
            navBackStackEntry?.destination?.route.toString()
                .replace(ROUTE_PREFIX, "")
        }"
    )

    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { screen ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(screen.route::class)
        } == true
    }

    // Show the Bottom Bar only if current destination is a bottom bar one (i.e. Authenticated)
    if (bottomBarDestination) {
        ActualNavBar(screens, navController, currentDestination, modifier)
    }
}

@Composable
private fun ActualNavBar(
    screens: Set<BottomNavItem>,
    navController: NavController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets, modifier = modifier) {

        screens.forEach { screen ->
            NavigationBarItem(
                label = { Text(screen.label) },
                icon = {
                    screen.icon?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = null
                        )
                    }
                },
                selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(screen.route::class)
                } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(),
            )
        }
    }
}

@Composable
@Preview
fun FreyzaNavBarPreview() {
    FreyzaEmployeeTheme {
        ActualNavBar(
            setOf(BottomNavItem.Home, BottomNavItem.TravelPlan, BottomNavItem.ExpenseHistory),
            rememberNavController(),
            NavDestination("")
        )
    }
}
