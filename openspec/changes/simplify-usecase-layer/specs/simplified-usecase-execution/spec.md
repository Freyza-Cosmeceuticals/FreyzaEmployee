## ADDED Requirements

### Requirement: UseCase as a Plain Class
The domain layer SHALL implement business logic as plain classes (without interfaces) that return a `Result<T>`.

#### Scenario: UseCase execution
- **WHEN** a UseCase class is called (via `invoke`)
- **THEN** it SHALL return a `Result.Success`, `Result.Error`, or `Result.Loading`

#### Scenario: Call site simplicity
- **WHEN** a ViewModel calls a UseCase
- **THEN** it SHALL be able to invoke the class directly as a function.
