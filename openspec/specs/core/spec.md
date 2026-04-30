# Core Specification

## Overview
Core utilities, state management, and shared types.

## Result Wrapper

### Result<T>
- **File**: `core/Result.kt`
- Sealed class for UseCase return types

```kotlin
sealed class Result<T>(val data: T? = null, val message: String? = null) {
  class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
  class Success<T>(data: T? = null) : Result<T>(data)
  class Loading<T>(data: T? = null) : Result<T>(data)
}
```

## UI State

### UIState<T>
- **File**: `core/UIState.kt`
- Sealed class for ViewModel state

```kotlin
sealed class UIState<T>(val data: T? = null, val message: String? = null) {
  class Idle<T>(data: T? = null) : UIState<T>(data)
  class Loading<T>(data: T? = null, message: String? = null) : UIState<T>(data, message)
  class Ready<T>(data: T, message: String? = null) : UIState<T>(data, message)
  class Error<T>(message: String, data: T? = null) : UIState<T>(data, message)
}
```

## Auth Response

### AuthResponse
- **File**: `core/AuthResponse.kt`

```kotlin
sealed class AuthResponse {
  class Success(val userInfo: UserInfo) : AuthResponse()
  class Error(val message: String) : AuthResponse()
  object Logout : AuthResponse()
}
```

## Session Manager

### SessionManager
- **File**: `core/state/SessionManager.kt`
- Stores current authenticated user

```kotlin
class SessionManager {
  val currentEmployee: MutableStateFlow<User?> = MutableStateFlow(null)
}
```

## Constants

### Constants
- **File**: `core/Constants.kt`
- `TIMEZONE = "Asia/Kolkata"`
- `NUM_RECENT_DAILY_REPORTS`

## AppConfig

### AppConfig
- **File**: `core/AppConfig.kt`

```kotlin
data class AppConfig(
  val supabaseUrl: String,
  val supabasePublishableKey: String
)
```

## Utilities

### Logger
- **File**: `core/util/Logger.kt`
- Wrapper around Timber

### DateFormatter
- **File**: `core/util/DateFormatter.kt`

### SharedPreferencesHelper
- **File**: `core/util/SharedPreferencesHelper.kt`

### Misc
- **File**: `core/util/Misc.kt`