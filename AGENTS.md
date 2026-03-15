# AGENTS.md - Freyza Employee App

This document provides guidance for agentic coding agents working in this repository.

## Project Overview

Android Kotlin app using Jetpack Compose with Clean Architecture (domain/data/presentation layers). The app is an employee tracking application for field sales teams to log daily visits, travel plans, and expenses.

## Tech Stack

- **Language**: Kotlin 2.3.0
- **UI**: Jetpack Compose with Material 3
- **DI**: Koin 4.1.1
- **Backend**: Supabase (PostgreSQL + Auth)
- **Architecture**: Clean Architecture with MVVM
- **Build Flavors**: dev, preview, prod

---

## Build Commands

### Build Debug/Release APKs

```bash
# Dev builds (applicationId: com.freyza.employee.dev)
./gradlew devDebugBuild      # Debug APK
./gradlew devReleaseBuild    # Release APK (requires signing config)

# Preview builds (applicationId: com.freyza.employee.preview)
./gradlew previewDebugBuild
./gradlew previewReleaseBuild

# Prod builds (applicationId: com.freyza.employee)
./gradlew prodDebugBuild
./gradlew prodReleaseBuild
```

### Other Commands

```bash
# Clean build
./gradlew clean

# Build with verbose output
./gradlew devDebugBuild --info

# Check dependencies
./gradlew dependencies
```

---

## Code Style Guidelines

### Formatting
- **Indentation**: 2 spaces (matches existing codebase)
- **Line length**: No strict limit, but prefer ~100 chars
- **Kotlin code style**: `official` (set in gradle.properties)

### Naming Conventions
- **Classes/Packages**: `PascalCase` (e.g., `HomeViewModel`, `DailyReport`)
- **Functions/Properties**: `camelCase` (e.g., `loadCurrentDailyReport`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `TIMEZONE`)
- **Enums**: `PascalCase` with `UPPER_SNAKE_CASE` values (e.g., `DayType.WORK`)
- **File names**: `PascalCase.kt` (e.g., `HomeViewModel.kt`)

### Imports
- Alphabetically sorted within groups
- Package-first ordering (android, kotlin, androidx, other)
- No wildcard imports

### Architecture Patterns

#### 1. UseCase Pattern
All business logic goes through UseCases. They follow this pattern:

```kotlin
interface UseCase<Input, Output> {
    suspend fun execute(input: Input): Output
}

// Output is always a sealed class
class Input { ... }
sealed class Output {
    class Success(...) : Output()
    class Failure(...) : Output()
}
```

Example: `GetTodayDailyReportUseCase`, `CreateVisitUseCase`

#### 2. Repository Pattern
- Interface in domain layer (`domain/repository/`)
- Implementation in data layer (`data/repository/`)
- Implementation named with `Impl` suffix

#### 3. Result/State Pattern
Two main state wrappers used throughout:

```kotlin
// For UseCase results (domain layer)
sealed class Result<T>(val data: T? = null, val message: String? = null) {
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Success<T>(data: T? = null) : Result<T>(data)
    class Loading<T>(data: T? = null) : Result<T>(data)
}

// For UI state (presentation layer)
sealed class UIState<T> {
    object Idle : UIState<Nothing>()
    class Loading<T>(val data: T? = null, val message: String? = null) : UIState<T>()
    class Ready<T>(val data: T) : UIState<T>()
    class Error<T>(val message: String, val data: T? = null) : UIState<T>()
}
```

#### 4. ViewModel Pattern
- Extend `androidx.lifecycle.ViewModel`
- Use `kotlinx.coroutines.flow.MutableStateFlow` for state
- Expose `StateFlow` with `stateIn()` for UI collection
- Use companion object for TAG constant

```kotlin
class HomeViewModel(...) : ViewModel() {
    companion object { const val TAG = "HomeViewModel" }
    
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.onStart { refresh() }.stateIn(...)
}
```

#### 5. Koin DI
- Define modules in `core/di/AppModule.kt`
- Use `single<>`, `factory<>`, `viewModel<>` for different lifetimes
- ViewModels with constructor params use `parametersOf()`

```kotlin
val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get(), ...) }
    viewModel { (param: Type) -> MyViewModel(param, get()) }
}
```

---

## Screen Data Flow

### 1. Login Screen (`LoginViewModel` + `LoginScreen`)
- **Route**: `NavRoutes.Unauthenticated.Login`
- **Data Flow**:
  - User enters email/password
  - `LoginViewModel.login()` calls `LoginUseCase`
  - On success: Navigate to Authenticated graph, session stored in `SessionManager`
- **Key Classes**: `LoginViewModel`, `LoginUseCase`, `AuthenticationRepository`

### 2. Home Screen (`HomeViewModel` + `HomeScreenRoute`)
- **Route**: `NavRoutes.Authenticated.Home`
- **Purpose**: Today's summary dashboard
- **Data Shown**:
  - Current travel plan (monthly overview)
  - Today's travel plan entry (route + locations)
  - Today's daily report (day type, route, visits count)
  - Available routes list
- **Data Flow**:
  - `HomeViewModel.refresh()` loads all data in parallel
  - Uses `GetCurrentTravelPlanUseCase`, `GetTodayDailyReportUseCase`, `GetAllRoutesWithLocationUseCase`
  - User can create today's report if none exists (bottom sheet)
- **Key State**: `HomeScreenUiState` - holds `currentTravelPlan`, `todayTravelPlanEntry`, `currentDailyReport`, `routes`

### 3. Travel Plan Screen (`TravelPlanViewModel` + `TravelPlanScreenRoute`)
- **Route**: `NavRoutes.Authenticated.TravelPlan`
- **Purpose**: View/edit monthly travel plan
- **Data Shown**:
  - Current travel plan (for current/next month)
  - List of travel plan entries (each day with day type and route)
  - Calendar view using kizitonwose.calendar library
- **Data Flow**:
  - `TravelPlanViewModel.loadCurrentTravelPlan()` fetches plan
  - User can modify entries by selecting day and changing day type/route
  - Uses `GetCurrentTravelPlanUseCase`, `GetTravelPlanEntriesUseCase`
- **Key State**: `TravelPlanUiState` - holds `currentTravelPlan`, `travelPlanEntries`, `selectedRoute`

### 4. Daily Reports Screen (`DailyReportViewModel` + `DailyReportScreenRoute`)
- **Route**: `NavRoutes.Authenticated.DailyReports`
- **Purpose**: View all daily reports with visits
- **Data Shown**:
  - List of all daily reports (with date, day type, lock status)
  - Routes for each report
  - Expandable to show visits within each report
- **Data Flow**:
  - `DailyReportViewModel.loadAllReports()` fetches all reports
  - Reports marked as `locked` cannot be edited
  - Uses `GetAllDailyReportsUseCase`
- **Key State**: `DailyReportUiState` - holds `dailyReports`, `routes`

### 5. Add Visit Screen (`AddVisitViewModel` + `AddVisitScreenRoute`)
- **Route**: `NavRoutes.Authenticated.AddVisit(type, reportId, employeeId)`
- **Purpose**: Add a new visit to a daily report
- **Data Required** (passed via navigation params):
  - `visitType`: Type of visit (Doctor, Chemist, Stockist, etc.)
  - `reportId`: The daily report to add visit to
  - `employeeId`: Current employee ID
- **Data Flow**:
  - User fills visit details (name, remarks, etc.)
  - `AddVisitViewModel.createVisit()` calls `CreateVisitUseCase`
  - On success: Navigate back with `created=true` flag
- **Key State**: `AddVisitUiState` - holds `visitType`, `creationState`

### 6. Profile Screen (`ProfileViewModel` + `ProfileScreenRoute`)
- **Route**: `NavRoutes.Authenticated.Profile`
- **Purpose**: Display user info and logout
- **Data Shown**:
  - User name, email, phone
  - Role (EMPLOYEE/ADMIN), status, tier (FSO/TABM/ASM)
  - Joining date, HQ ID
- **Data Flow**:
  - Loads from `MainUiState.user` (set at app startup)
  - `ProfileViewModel.logout()` calls `LogoutUseCase`
  - On success: Navigate to unauthenticated graph
- **Key State**: `ProfileScreenUiState` - holds `user`

---

## Domain Models

### Core Entities
- **User**: `id`, `name`, `email`, `phone`, `role`, `status`, `tier`, `hqId`, `joiningDate`
- **DailyReport**: `id`, `employeeId`, `date`, `dayType`, `routeId`, `ta`, `da`, `totalExpense`, `visits`, `locked`
- **TravelPlan**: `id`, `employeeId`, `month`, `entries`, `createdById`
- **TravelPlanEntry**: `id`, `tpId`, `date`, `dayType`, `routeId`
- **Route**: `id`, `srcLocId`, `destLocId`, `distanceKm`
- **Location**: `id`, `name`, `address`, `type`
- **Visit**: `id`, `reportId`, `type`, `name`, `remarks`, `createdAt`

### Enums
- **DayType**: `WORK`, `HOLIDAY`, `LEAVE`
- **UserRole**: `EMPLOYEE`, `ADMIN`
- **UserStatus**: `ACTIVE`, `REVOKED`
- **EmployeeTier**: `FSO`, `TABM`, `ASM`
- **VisitType**: Defined in `Visit.kt` (Doctor, Chemist, Stockist, etc.)

---

## Error Handling

- Use sealed `Result<T>` class from `core/Result.kt` in UseCases
- Use sealed `UIState<T>` class from `core/UIState.kt` in ViewModels
- Log errors with `Logger` utility (wraps Timber)
- Show user-friendly error messages via Snackbar

---

## Important Files

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | App config, flavors, dependencies |
| `gradle/libs.versions.toml` | Version catalog |
| `app/src/main/java/com/freyza/employee/core/di/AppModule.kt` | Koin DI modules |
| `app/src/main/java/com/freyza/employee/presentation/nav/NestedNavigations.kt` | Navigation graph |
| `app/src/main/java/com/freyza/employee/core/Result.kt` | Result wrapper |
| `app/src/main/java/com/freyza/employee/core/UIState.kt` | UI state wrapper |
| `app/src/main/java/com/freyza/employee/FreyzaEmployeeApp.kt` | Root composable |
| `app/src/main/java/com/freyza/employee/MainActivity.kt` | Entry point |
