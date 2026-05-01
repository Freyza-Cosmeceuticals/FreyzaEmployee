## Context

The project currently follows a strict Clean Architecture pattern where every UseCase has a dedicated interface with its own `Input` and `Output` sealed classes. While this provides strong typing, it introduces significant boilerplate and redundant mapping logic in the implementation layer.

## Goals / Non-Goals

**Goals:**
- Reduce the number of files and classes in the domain layer.
- Eliminate redundant mapping between repository `Result` and use-case `Output`.
- Maintain a consistent, type-safe way to handle success, error, and loading states.

**Non-Goals:**
- Removing the UseCase layer entirely (it still serves as the entry point for business logic).
- Changing the `Result` or `UIState` wrapper definitions.

## Decisions

### 1. Plain Class Pattern
Remove the Interface/Implementation split and the base `UseCase` interface. Each UseCase will be a single plain class.
- **Rationale**: Interfaces for UseCases provided no real polymorphic value in this project and only added file-count and mapping boilerplate.
- **Alternative**: Keeping the base interface. Rejected because the "contract" is implicitly handled by the class's own function signature.

### 2. Use of `invoke` Operator
The primary business logic will be placed in an `operator fun invoke(...)` method.
- **Rationale**: This allows the UseCase to be called as a function (e.g., `getUserUseCase()`), making the ViewModel code more concise.

### 3. Unified Output Wrapper
All UseCases will return the global `Result<T>` wrapper.
- **Rationale**: Eliminates the need for per-use-case sealed output classes.

### 4. Direct Repository Returns
UseCase classes will return repository results directly when no additional domain logic is required.
- **Rationale**: Reduces mapping boilerplate.

### 5. DI Simplification
Update Koin modules to use `factory { GetUserUseCase(get()) }` instead of `factory<GetUserUseCase> { GetUserUseCaseImpl(get()) }`.
- **Rationale**: Since the interface is gone, we register the concrete class directly.

## Risks / Trade-offs

- **[Breaking Change]** → All ViewModels using UseCases will break. Mitigation: Perform a systematic refactor of all ViewModels immediately after the interface change.
- **[Loss of Specificity]** → Some UseCases might have had highly specific `Output.Failure` subclasses. Mitigation: Use the `data` field in `Result.Error` to pass specific error objects if needed.
