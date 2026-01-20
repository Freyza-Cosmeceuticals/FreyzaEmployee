package com.freyza.employee.presentation.nav

import com.freyza.employee.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {
  @Serializable
  sealed class Unauthenticated() : NavRoutes() {
    @Serializable
    object NavigationRoute : Unauthenticated()

    @Serializable
    object Login : Unauthenticated()

    @Serializable
    object Register : Unauthenticated()
  }

  @Serializable
  sealed class Authenticated() : NavRoutes() {
    @Serializable
    object NavigationRoute : Authenticated()

    @Serializable
    object Home : Authenticated()

    @Serializable
    object TravelPlan : Authenticated()

    @Serializable
    object Profile : Authenticated()

    @Serializable
    object ExpenseHistory : Authenticated()

    @Serializable
    object AddExpense : Authenticated()

    @Serializable
    data class ExpenseDetail(val expenseId: String) : Authenticated()
  }
}

sealed class BottomNavItem(val route: NavRoutes, val icon: Int?, val label: String) {
  object Home : BottomNavItem(
    NavRoutes.Authenticated.Home,
    icon = R.drawable.home_24px,
    label = "Today"
  )

  object TravelPlan :
    BottomNavItem(
      NavRoutes.Authenticated.TravelPlan,
      icon = R.drawable.calendar_month_24px,
      label = "Travel Plan"
    )

  object ExpenseHistory : BottomNavItem(
    NavRoutes.Authenticated.ExpenseHistory,
    icon = R.drawable.empty_dashboard_24px,
    label = "Reports",
  )

  object Profile : BottomNavItem(
    NavRoutes.Authenticated.Profile,
    icon = R.drawable.line_end_arrow_notch_24px,
    label = "Profile"
  )
}
