# Travel Plans Specification

## Overview
Travel plan management screen for viewing monthly travel plans and entries.

## ViewModel

### TravelPlanViewModel
- **File**: `presentation/ui/viewmodels/TravelPlanViewModel.kt`
- **Tag**: `"TravelPlanViewModel"`

## State

### TravelPlanUiState
- **File**: `presentation/ui/state/TravelPlanUiState.kt`

```kotlin
data class TravelPlanUiState(
  val currentTravelPlan: UIState<TravelPlan?> = UIState.Idle(),
  val travelPlanEntries: UIState<List<TravelPlanEntry>> = UIState.Idle(),
  val routes: List<RouteWithLocation> = emptyList(),
  val selectedRoute: UIState<RouteWithLocation?> = UIState.Idle(),
)
```

## UseCases

### GetCurrentTravelPlanUseCase
- **Input**: employeeId
- **Output**: Success(travelPlan) | Failure(message)

### GetTravelPlanEntriesUseCase
- **Input**: tpId (travel plan ID)
- **Output**: Success(entries) | Failure(message)

### GetTodayTravelPlanEntryUseCase
- **Input**: tpId
- **Output**: Success(entry) | Failure(message)

### GetAllRoutesWithLocationUseCase
- **Input**: none
- **Output**: Success(routes) | Failure(message)

## Functions

### loadCurrentTravelPlan(employeeId)
- Fetches current travel plan for employee
- Then loads all entries

### loadTravelPlanEntries(tpId)
- Loads all entries for a travel plan

### loadSelectedPlanEntryRoute(tpEntryId)
- Sets selected route from cached routes list

### loadAllRoutes()
- Fetches all routes with location data

## Domain Models

### TravelPlan
- id, employeeId, month, year, entries, createdById

### TravelPlanEntry
- id, tpId, date, dayType, routeId

## Route

- `NavRoutes.Authenticated.TravelPlan`

## DI Registration

```kotlin
viewModel { TravelPlanViewModel(get(), get(), get(), get(), get()) }
```