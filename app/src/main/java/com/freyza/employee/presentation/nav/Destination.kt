package com.freyza.employee.presentation.nav

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed class NavigationRoutes {
    @Serializable
    sealed class Unauthenticated() : NavigationRoutes() {
        @Serializable
        object NavigationRoute : Unauthenticated()

        @Serializable
        object Login : Unauthenticated()

        @Serializable
        object Register : Unauthenticated()
    }

    @Serializable
    sealed class Authenticated() : NavigationRoutes() {
        @Serializable
        object NavigationRoute : Authenticated()

        @Serializable
        object Home : Authenticated()

        @Serializable
        object ExpenseHistory : Authenticated()

        @Serializable
        object AddExpense : Authenticated()

        @Serializable
        data class ExpenseDetail(val expenseId: String) : Authenticated()
    }
}

sealed class BottomNavItem(val route: NavigationRoutes, val icon: ImageVector?, val label: String) {
    object ExpenseHistory : BottomNavItem(
        NavigationRoutes.Authenticated.ExpenseHistory,
        icon = null,
        label = "History",
    )

    object Home : BottomNavItem(NavigationRoutes.Authenticated.Home, icon = null, label = "Home")
}
