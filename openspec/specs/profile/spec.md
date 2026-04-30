# Profile Specification

## Overview
Profile screen displaying user info and logout functionality.

## ViewModel

### ProfileViewModel
- **File**: `presentation/ui/viewmodels/ProfileViewModel.kt`
- **Tag**: `"ProfileViewModel"`

## State

### ProfileScreenUiState
- **File**: `presentation/ui/state/ProfileScreenUiState.kt`

```kotlin
data class ProfileScreenUiState(
  val user: UIState<User> = UIState.Idle(),
)
```

## User Data

### User Model
- **File**: `domain/model/User.kt`

```kotlin
data class User(
  val id: String,
  val name: String,
  val email: String,
  val phone: String?,
  val role: UserRole,
  val status: UserStatus,
  val tier: EmployeeTier,
  val hqId: String?,
  val joiningDate: LocalDate?,
)
```

### Enums
- **UserRole**: EMPLOYEE, ADMIN
- **UserStatus**: ACTIVE, REVOKED
- **EmployeeTier**: FSO, TABM, ASM

## Functions

### logout()
- Calls `LogoutUseCase`
- On success: navigates to unauthenticated route
- Uses `MainViewModel.handleLogout()`

## Route

- `NavRoutes.Authenticated.Profile`

## DI Registration

```kotlin
viewModel { ProfileViewModel(get(), get()) }
```