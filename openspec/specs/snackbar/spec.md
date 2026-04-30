# Snackbar Specification

## Overview
In-app snackbar notifications for user feedback.

## Components

### SnackbarManager
- **File**: `core/util/SnackbarManager.kt`
- Manages snackbar state with `MutableStateFlow<FreyzaSnackbarVisuals?>`

```kotlin
class SnackbarManager {
  val snackbarVisuals: StateFlow<FreyzaSnackbarVisuals?>
  
  fun showSuccess(message: String)
  fun showError(message: String)
  fun showInfo(message: String)
  fun clear()
}
```

### FreyzaSnackbarVisuals
- **File**: `core/FreyzaSnackbarVisuals.kt`
- Sealed class for snackbar types

```kotlin
sealed class FreyzaSnackbarVisuals {
  data class Success(val message: String, val action: SnackbarAction? = null)
  data class Error(val message: String, val action: SnackbarAction? = null)
  data class Info(val message: String, val action: SnackbarAction? = null)
}
```

## Composables

### FreyzaSnackbarHost
- **File**: `presentation/ui/composables/FreyzaSnackbarHost.kt`
- Host for displaying snackbars

```kotlin
@Composable
fun FreyzaSnackbarHost(
  snackbarManager: SnackbarManager,
  modifier: Modifier = Modifier
)
```

## Usage Pattern

### In ViewModels
```kotlin
class HomeViewModel(private val snackbarManager: SnackbarManager) {
  fun someAction() {
    snackbarManager.showSuccess("Report created successfully")
    snackbarManager.showError("Failed to load data")
  }
}
```

### In Composable
```kotlin
FreyzaSnackbarHost(snackbarManager = get())
```

## Integration

### DI Registration
```kotlin
single<SnackbarManager> { SnackbarManager() }
```

### ViewModel Injection
- Injected via constructor in ViewModels
- Used in: HomeViewModel, DailyReportViewModel, AddVisitViewModel