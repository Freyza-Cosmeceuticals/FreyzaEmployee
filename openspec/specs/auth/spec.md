# Auth Specification

## Overview
Authentication system using Supabase Auth (PKCE flow).

## UseCases

### LoginUseCase
- **Interface**: `domain/usecase/auth/LoginUseCase.kt`
- **Input**: `email: String, password: String`
- **Output**: `Success(UserInfo) | Failure(message) | Logout`

### RegisterUseCase
- **Interface**: `domain/usecase/auth/RegisterUseCase.kt`
- **Input**: `name, email, password`
- **Output**: `Success | Failure | Logout`

### LogoutUseCase
- **Interface**: `domain/usecase/auth/LogoutUseCase.kt`
- **Output**: `Success | Failure`

### LoginWithGoogleUseCase
- **Interface**: `domain/usecase/auth/LoginWithGoogleUseCase.kt`
- **Output**: `Success | Failure | Logout`

## Repository

### AuthenticationRepository
- **Interface**: `domain/repository/AuthenticationRepository.kt`
- **Impl**: `data/repository/AuthenticationRepositoryImpl.kt`
- **Methods**:
  - `suspend fun login(email, password): AuthResponse`
  - `suspend fun register(name, email, password): AuthResponse`
  - `suspend fun loginWithGoogle(): AuthResponse`
  - `suspend fun logout(): AuthResponse`

## Auth Response Types

```kotlin
sealed class AuthResponse {
  class Success(val userInfo: UserInfo) : AuthResponse()
  class Error(val message: String) : AuthResponse()
  object Logout : AuthResponse()
}
```

## ViewModel

### LoginViewModel
- **File**: `presentation/ui/viewmodels/LoginViewModel.kt`
- **State**: `LoginScreenUiState`
- **Functions**: `login(email, password)`, `loginWithGoogle()`

## Route

- `NavRoutes.Unauthenticated.Login`
- `NavRoutes.Unauthenticated.Register`

## DI Registration

```kotlin
single<LoginUseCase> { LoginUseCaseImpl(get()) }
single<RegisterUseCase> { RegisterUseCaseImpl(get()) }
single<LoginWithGoogleUseCase> { LoginWithGoogleUseCaseImpl(get()) }
single<LogoutUseCase> { LogoutUseCaseImpl(get()) }
```

## Session Management

- `SessionManager` in `core/state/SessionManager.kt`
- Stores current employee in `MutableStateFlow<User?>`
- PKCE flow with scheme: `app`, host: `supabase.com`