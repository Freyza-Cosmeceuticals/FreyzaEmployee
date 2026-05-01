## 1. Domain Core

- [x] 1.1 Delete `UseCase.kt` base interface

## 2. Domain UseCases

- [x] 2.1 Merge UseCase interfaces and Impl classes into single plain classes
- [x] 2.2 Implement `operator fun invoke` for primary logic in each UseCase
- [x] 2.3 Remove all `Input` and `Output` sealed classes
- [x] 2.4 Return `Result<T>` directly from repository calls where applicable

## 3. Dependency Injection

- [x] 3.1 Update `AppModule.kt` (or relevant DI modules) to register UseCase classes directly instead of interface bindings

## 4. Presentation Layer

- [x] 4.1 Update ViewModels to call UseCase classes as functions
- [x] 4.2 Update ViewModel `when` blocks to handle the global `Result` wrapper

## 5. Verification

- [x] 5.1 Run `./gradlew assembleDebug` to verify no compilation errors
