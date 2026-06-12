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
