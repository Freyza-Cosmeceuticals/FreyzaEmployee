# Daily Reports Specification

## Overview
Daily reports screen for viewing and locking employee daily reports with visits.

## ViewModel

### DailyReportViewModel
- **File**: `presentation/ui/viewmodels/DailyReportViewModel.kt`
- **Tag**: `"DailyReportViewModel"`

## State

### DailyReportUiState
- **File**: `presentation/ui/state/DailyReportUiState.kt`

```kotlin
data class DailyReportUiState(
  val dailyReports: UIState<List<DailyReport>> = UIState.Idle(),
  val routes: UIState<List<RouteWithLocation>> = UIState.Idle(),
  val lockingState: UIState<Unit> = UIState.Idle(),
)
```

## UseCases

### GetRecentDailyReportsUseCase
- **Input**: limit, employeeId, includeVisits
- **Output**: Success(reports) | Failure(message)

### LockReportUseCase
- **Input**: reportId
- **Output**: Success | Failure(message)

### GetAllRoutesWithLocationUseCase
- **Output**: Success(routes) | Failure(message)

## Functions

### refresh()
- Loads all daily reports and routes

### loadAllDailyReports(employeeId)
- Fetches recent daily reports with visits

### lockReport(reportId)
- Locks a report to prevent editing

### loadAllRoutes()
- Fetches all routes with location data

## Domain Model

### DailyReport
```kotlin
data class DailyReport(
  val id: String,
  val employeeId: String,
  val date: LocalDate,
  val dayType: DayType,
  val routeId: String?,
  val ta: Double?,
  val da: Double?,
  val totalExpense: Double?,
  val visits: List<Visit>,
  val locked: Boolean,
  val lockedAt: Instant?,
  val createdAt: Instant,
  val updatedAt: Instant?,
)
```

## DayType Enum
- WORK
- HOLIDAY
- LEAVE

## Route

- `NavRoutes.Authenticated.DailyReports`

## DI Registration

```kotlin
viewModel { DailyReportViewModel(get(), get(), get(), get(), get()) }
```