# Core UI Specification

## Overview
Theme and reusable UI components.

## Theme

### FreyzaEmployeeTheme
- **File**: `presentation/ui/theme/Theme.kt`
- Uses Material 3 dynamic colors (Android 12+)
- Dark/Light mode support via `isSystemInDarkTheme()`

### ColorScheme
- **File**: `presentation/ui/theme/Color.kt`
- Primary: Purple40/Purple80
- Secondary: PurpleGrey40/PurpleGrey80
- Tertiary: Pink40/Pink80

### Typography
- **File**: `presentation/ui/theme/Type.kt`

## Reusable Composables

### FreyzaAppBar
- **File**: `presentation/ui/composables/FreyzaAppBar.kt`

### FreyzaBottomNavBar
- **File**: `presentation/ui/composables/FreyzaBottomNavBar.kt`

### FreyzaFabButton
- **File**: `presentation/ui/composables/FreyzaFabButton.kt`

### FreyzaSnackbarHost
- **File**: `presentation/ui/composables/FreyzaSnackbarHost.kt`

### Skeleton
- **File**: `presentation/ui/composables/Skeleton.kt`

### LoadingIndicator
- **File**: `presentation/ui/composables/LoadingIndicator.kt`

### RouteItem
- **File**: `presentation/ui/composables/RouteItem.kt`

### VisitBadge
- **File**: `presentation/ui/composables/VisitBadge.kt`

### ReportLockedBadge
- **File**: `presentation/ui/composables/ReportLockedBadge.kt`

### VersionInfo
- **File**: `presentation/ui/composables/VersionInfo.kt`

### TimedGreeting
- **File**: `core/util/TimedGreeting.kt`

## Snackbar System

### SnackbarManager
- **File**: `core/util/SnackbarManager.kt`
- Manages snackbar state with `MutableStateFlow<FreyzaSnackbarVisuals?>`

### FreyzaSnackbarVisuals
- **File**: `core/FreyzaSnackbarVisuals.kt`

## State Pattern

### UIState<T>
- **File**: `core/UIState.kt`

```kotlin
sealed class UIState<T>(val data: T? = null, val message: String? = null) {
  class Idle<T>(data: T? = null) : UIState<T>(data)
  class Loading<T>(data: T? = null, message: String? = null) : UIState<T>(data, message)
  class Ready<T>(data: T, message: String? = null) : UIState<T>(data, message)
  class Error<T>(message: String, data: T? = null) : UIState<T>(data, message)
}
```

## Helper Composables

### AuthenticatedRouteWrapper
- **File**: `presentation/ui/authenticated/AuthenticatedRouteWrapper.kt`