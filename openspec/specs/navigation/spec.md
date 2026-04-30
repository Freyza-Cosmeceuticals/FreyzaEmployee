# Navigation Specification

## Overview
Jetpack Compose type-safe navigation.

## NavRoutes

### Destination.kt
- **File**: `presentation/nav/Destination.kt`

```kotlin
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
    object Profile : Authenticated()
    @Serializable
    data class AddVisit(val type: VisitType, val reportId: String, val employeeId: String) : Authenticated()
  }
}
```

## Bottom Navigation

### BottomNavItem
```kotlin
sealed class BottomNavItem(val route: NavRoutes, val icon: Int?, val label: String) {
  object Home : BottomNavItem(NavRoutes.Authenticated.Home, R.drawable.home_24px, "Today")
  object TravelPlan : BottomNavItem(NavRoutes.Authenticated.TravelPlan, R.drawable.calendar_month_24px, "Travel Plan")
  object DailyReports : BottomNavItem(NavRoutes.Authenticated.DailyReports, R.drawable.empty_dashboard_24px, "Reports")
  object Profile : BottomNavItem(NavRoutes.Authenticated.Profile, R.drawable.account_circle_24px, "Profile")
}
```

## Nested Navigation

### NestedNavigations.kt
- **File**: `presentation/nav/NestedNavigations.kt`
- Two graphs: `unauthenticatedGraph` and `authenticatedGraph`

### unauthenticatedGraph
- Start: `NavRoutes.Unauthenticated.Login`
- Routes: Login, Register

### authenticatedGraph
- Start: `NavRoutes.Authenticated.Home`
- Routes: Home, TravelPlan, DailyReports, Profile, AddVisit
- Uses `koinViewModel<ViewModel>()` for each screen

## Navigation Functions

### navigateToTab(route)
- Uses `popUpTo` with `saveState=true` for tab reselection

### AddVisit Navigation
- Uses `savedStateHandle` to pass `created` flag back to DailyReports screen

## Dependencies
- `androidx.navigation:navigation-compose`
- `org.koin-androidx.compose:koin-androidx-compose`