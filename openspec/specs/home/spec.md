# Home Specification

## Overview
Home screen dashboard showing today's travel plan entry, daily report summary, and available routes.

## ViewModel

### HomeViewModel
- **File**: `presentation/ui/viewmodels/HomeViewModel.kt`
- **Tag**: `"HomeViewModel"`

## State

### HomeScreenUiState
- **File**: `presentation/ui/state/HomeScreenUiState.kt`

```kotlin
data class HomeScreenUiState(
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val todayTravelPlanEntry: UIState<TravelPlanEntry?> = UIState.Idle(),
  val todayPlanEntryRoute: UIState<RouteWithLocation?> = UIState.Idle(),
  val currentDailyReport: UIState<DailyReport?> = UIState.Idle(),
  val routes: UIState<List<RouteWithLocation>> = UIState.Idle(),
  val todayReportDayType: UIState<DayType> = UIState.Idle(),
  val todayReportRoute: UIState<RouteWithLocation?> = UIState.Idle(),
)
```

## Functions

### refresh()
- Loads all data: daily report, routes, travel plan

### loadCurrentTravelPlan(employeeId)
- Fetches current month's travel plan

### loadCurrentDailyReport(employeeId)
- Fetches today's daily report
- Creates new report if none exists

### createCurrentDailyReport(dayType, routeId)
- Creates today's daily report with selected day type and route

### loadAllRoutes()
- Fetches all routes with location data

## UseCases Used

- `GetCurrentTravelPlanUseCase`
- `GetTodayTravelPlanEntryUseCase`
- `GetRouteUseCase`
- `GetLocationUseCase`
- `GetAllRoutesWithLocationUseCase`
- `GetTodayDailyReportUseCase`
- `CreateTodayDailyReportUseCase`

## Route

- `NavRoutes.Authenticated.Home`

## DI Registration

```kotlin
viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
```