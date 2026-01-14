package com.freyza.employee.presentation.nav

import com.freyza.employee.R
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
        object TravelPlan : Authenticated()

        @Serializable
        object ExpenseHistory : Authenticated()

        @Serializable
        object AddExpense : Authenticated()

        @Serializable
        data class ExpenseDetail(val expenseId: String) : Authenticated()
    }
}

sealed class BottomNavItem(val route: NavigationRoutes, val icon: Int?, val label: String) {
    object Home : BottomNavItem(
        NavigationRoutes.Authenticated.Home,
        icon = R.drawable.home_24px,
        label = "Home"
    )

    object TravelPlan :
        BottomNavItem(
            NavigationRoutes.Authenticated.TravelPlan,
            icon = R.drawable.calendar_month_24px,
            label = "Travel Plan"
        )

    object ExpenseHistory : BottomNavItem(
        NavigationRoutes.Authenticated.ExpenseHistory,
        icon = R.drawable.empty_dashboard_24px,
        label = "History",
    )

}
