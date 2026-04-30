# DI Specification

## Overview
Koin dependency injection modules.

## AppModule

### appModule
- **File**: `core/di/AppModule.kt`

```kotlin
single<AppConfig> { AppConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY) }
single<SessionManager> { SessionManager() }
single<SnackbarManager> { SnackbarManager() }
```

## Repository Module

### repositoryModule

```kotlin
single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
single<TravelPlanRepository> { TravelPlanRepositoryImpl(get()) }
single<DailyReportRepository> { DailyReportRepositoryImpl(get()) }
single<UserRepository> { UserRepositoryImpl(get(), get()) }
single<LocationRepository> { LocationRepositoryImpl(get()) }
single<RouteRepository> { RouteRepositoryImpl(get()) }
```

## Supabase Module

### supabaseModule

```kotlin
single<SupabaseClient> { createSupabaseClient(...) }
single<Auth> { get<SupabaseClient>().auth }
single<Postgrest> { get<SupabaseClient>().postgrest }
```

### Auth Config
- FlowType: PKCE
- Scheme: `app`
- Host: `supabase.com`
- Postgrest: PropertyConversionMethod.NONE

## UseCase Module

### useCaseModule

**Auth:**
```kotlin
single<LoginUseCase> { LoginUseCaseImpl(get()) }
single<RegisterUseCase> { RegisterUseCaseImpl(get()) }
single<LoginWithGoogleUseCase> { LoginWithGoogleUseCaseImpl(get()) }
single<LogoutUseCase> { LogoutUseCaseImpl(get()) }
```

**User:**
```kotlin
single<GetUserUseCase> { GetUserUseCaseImpl(get()) }
single<GetCurrentUserUseCase> { GetCurrentUserUseCaseImpl(get()) }
```

**TravelPlan:**
```kotlin
single<GetTravelPlanUseCase> { GetTravelPlanUseCaseImpl(get()) }
single<GetCurrentTravelPlanUseCase> { GetCurrentTravelPlanUseCaseImpl(get()) }
single<GetTodayTravelPlanEntryUseCase> { GetTodayTravelPlanEntryUseCaseImpl(get()) }
single<GetTravelPlanEntriesUseCase> { GetTravelPlanEntriesUseCaseImpl(get()) }
```

**DailyReport:**
```kotlin
single<GetTodayDailyReportUseCase> { GetTodayDailyReportUseCaseImpl(get()) }
single<CreateTodayDailyReportUseCase> { CreateTodayDailyReportUseCaseImpl(get()) }
single<GetRecentDailyReportsUseCase> { GetRecentDailyReportsUseCaseImpl(get()) }
single<CreateVisitUseCase> { CreateVisitUseCaseImpl(get()) }
single<LockReportUseCase> { LockReportUseCaseImpl(get()) }
```

**Location/Route:**
```kotlin
single<GetLocationUseCase> { GetLocationUseCaseImpl(get()) }
single<GetRouteUseCase> { GetRouteUseCaseImpl(get()) }
single<GetAllRoutesWithLocationUseCase> { GetAllRoutesWithLocationUseCaseImpl(get()) }
```

## ViewModel Module

### viewModelModule

```kotlin
viewModel { MainViewModel(get(), get(), get(), get()) }
viewModel { LoginViewModel(get(), get(), get()) }
viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
viewModel { TravelPlanViewModel(get(), get(), get(), get(), get()) }
viewModel { DailyReportViewModel(get(), get(), get(), get(), get()) }
viewModel { (visitType: VisitType, reportId: String, employeeId: String) ->
  AddVisitViewModel(visitType, reportId, employeeId, get(), get(), get())
}
viewModel { ProfileViewModel(get(), get()) }
```

## Usage

```kotlin
// In Composables
val vm = koinViewModel<HomeViewModel>()
val vm = koinViewModel<AddVisitViewModel>(parameters = { parametersOf(visitType, reportId, employeeId) })
```