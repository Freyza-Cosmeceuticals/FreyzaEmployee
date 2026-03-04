package com.freyza.employee.presentation.nav

import com.freyza.employee.R
import com.freyza.employee.domain.model.VisitType
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {
  @Serializable
  sealed class Unauthenticated : NavRoutes() {
    @Serializable
    object NavigationRoute : Unauthenticated()

    @Serializable
    object Login : Unauthenticated()

    @Serializable
    object Register : Unauthenticated()
  }

  @Serializable
  sealed class Authenticated : NavRoutes() {
    @Serializable
    object NavigationRoute : Authenticated()

    @Serializable
    object Home : Authenticated()

    @Serializable
    object TravelPlan : Authenticated()

    @Serializable
    object DailyReports : Authenticated()

    @Serializable
    class AddVisit(val type: VisitType) : Authenticated()

    @Serializable
    object Profile : Authenticated()
  }
}

sealed class BottomNavItem(val route: NavRoutes, val icon: Int?, val label: String) {
  object Home : BottomNavItem(
    NavRoutes.Authenticated.Home, icon = R.drawable.home_24px, label = "Today"
  )

  object TravelPlan : BottomNavItem(
    NavRoutes.Authenticated.TravelPlan, icon = R.drawable.calendar_month_24px, label = "Travel Plan"
  )

  object DailyReports : BottomNavItem(
    NavRoutes.Authenticated.DailyReports,
    icon = R.drawable.empty_dashboard_24px,
    label = "Reports",
  )

  object Profile : BottomNavItem(
    NavRoutes.Authenticated.Profile, icon = R.drawable.account_circle_24px, label = "Profile"
  )
}
