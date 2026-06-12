# AGENTS.md - Freyza Employee App

## Project Overview

Android Kotlin app using Jetpack Compose with Clean Architecture (domain/data/presentation layers). The app is an employee tracking application for field sales teams to log daily visits, travel plans, and expenses.

## Tech Stack

- Kotlin
- Jetpack compose with material 3
- Koin DI
- Supabase backend for Auth and Postgres
- MVVM Clean Architecture
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

Other standard gradle commands available

---

## Code Style Guidelines

### Formatting
- 2 spaces
- 100 chars in a line
- **Kotlin code style**: `official`
- Standard naming conventions

### Architecture Patterns

#### 1. UseCase Pattern
All business logic goes through UseCases.
Example: `GetTodayDailyReportUseCase`, `CreateVisitUseCase`

#### 2. Repository Pattern
- Interface in domain layer (`domain/repository/`)
- Implementation in data layer (`data/repository/`)
- Implementation named with `Impl` suffix

#### 3. ViewModel Pattern
- Extend `androidx.lifecycle.ViewModel`
- Use `kotlinx.coroutines.flow.MutableStateFlow` for state
- Expose `StateFlow` with `stateIn()` for UI collection
- Use companion object for TAG constant

#### 5. Koin DI
- Define modules in `core/di/AppModule.kt`
- Use `single<>`, `factory<>`, `viewModel<>` for different lifetimes
- ViewModels with constructor params use `parametersOf()`

---
