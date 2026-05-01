## MODIFIED Requirements

### Requirement: UseCase Pattern
UseCases are implemented as plain classes with an `invoke` operator.
```kotlin
class GetUserUseCase(private val repo: UserRepository) {
  suspend operator fun invoke(userId: String): Result<User> {
    return repo.getUser(userId)
  }
}
```

#### Scenario: UseCase implementation
- **WHEN** implementing a UseCase
- **THEN** it MUST be a plain class returning `Result<T>`

#### Scenario: ViewModel consumption
- **WHEN** ViewModel calls a UseCase
- **THEN** it MUST handle the `Result` sealed class (Success, Error, Loading) to update `UIState`
