# Clear Architecture Specification

## Overview
Three-layer Clean Architecture separating domain, data, and presentation layers.

## Layers

```
app/src/main/java/com/freyza/employee/
├── domain/          # Business logic (innermost)
├── data/            # Data access & network
└── presentation/    # UI layer (outermost)
```

## Domain Layer

### Purpose
Business logic, entities, use cases, repository interfaces.
No Android/Compose dependencies.

### Packages
- `domain/model/` - Entities (User, DailyReport, TravelPlan, Route, Location, Visit)
- `domain/usecase/` - UseCase interfaces and implementations
- `domain/repository/` - Repository interfaces

### UseCase Pattern
```kotlin
interface UseCase<Input, Output> {
  suspend fun execute(input: Input): Output
}

// Output is always sealed class
sealed class Output {
  class Success(...) : Output()
  class Failure(...) : Output()
}
```

### Repository Pattern
```kotlin
interface SomeRepository {
  suspend fun getData(): Result<Type>
}

// Implementation in data layer
class SomeRepositoryImpl(private val client: Postgrest) : SomeRepository
```

## Data Layer

### Purpose
Data access, network calls, DTOs, repository implementations.
Connects domain to Supabase.

### Packages
- `data/network/dto/` - Data transfer objects
- `data/mappers/` - DTO to Domain mappers
- `data/repository/` - Repository implementations

### Network
- Supabase client via `io.github.jan.supabase:postgrest-kt`
- DTOs use kotlinx.serialization

## Presentation Layer

### Purpose
UI, ViewModels, Composables, Navigation.
Android/Compose dependent.

### Packages
- `presentation/ui/viewmodels/` - ViewModels
- `presentation/ui/state/` - UI state classes
- `presentation/ui/composables/` - Composable functions
- `presentation/ui/theme/` - Theme (Color, Type, Theme)
- `presentation/nav/` - Navigation graph
- `presentation/ui/authenticated/` - Auth screens
- `presentation/ui/unauthenticated/` - Unauth screens

### ViewModel Pattern
```kotlin
class SomeViewModel(
  private val useCase: SomeUseCase,
  private val sessionManager: SessionManager,
) : ViewModel() {
  
  private val _uiState = MutableStateFlow(SomeUiState())
  val uiState = _uiState.onStart { loadData() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SomeUiState())
}
```

### State Pattern
```kotlin
sealed class UIState<T> {
  object Idle : UIState<Nothing>()
  class Loading<T>(val data: T? = null) : UIState<T>()
  class Ready<T>(val data: T) : UIState<T>()
  class Error<T>(val message: String) : UIState<T>()
}
```

## Dependency Flow

```
Presentation ──► Domain ──► Data
   ViewModel   UseCase   RepositoryImpl
       │          │            │
       └──────────┴────────────┘
              Dependency Injection (Koin)
```

## Data Flow

```
User Action → Composable Event → ViewModel → UseCase → Repository → Network
                ↑                                              │
                └──────────────── UI State ←──────────────────┘
```

## Core Utilities

- `core/Result.kt` - Result wrapper for UseCases
- `core/UIState.kt` - UI state wrapper
- `core/di/AppModule.kt` - Koin modules
- `core/state/SessionManager.kt` - Auth session
- `core/util/` - Logger, DateFormatter, SnackbarManager

## Build Flavors

- `dev` - com.freyza.employee.dev
- `preview` - com.freyza.employee.preview
- `prod` - com.freyza.employee