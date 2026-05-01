## Why

The current UseCase pattern requires a separate interface, an Input class, and a custom Output sealed class for every single business operation. This leads to excessive boilerplate, redundant mapping between repository `Result` types and use-case `Output` types, and an inflated number of files in the domain layer.

## What Changes

- **BREAKING**: Remove the `UseCase` base interface and all UseCase-specific interfaces.
- **BREAKING**: Remove all `Impl` classes for UseCases.
- Convert each UseCase into a single plain class that handles its own logic and return types.
- Use `operator fun invoke` for the primary execution logic to allow function-like calls in ViewModels.
- Use the global `Result<T>` wrapper for all outputs.
- Update all ViewModels to call UseCase classes directly.
- **BREAKING**: Update Koin DI modules to register UseCase classes directly instead of binding interfaces to implementations.

## Capabilities

### New Capabilities
- `simplified-usecase-execution`: A streamlined execution pattern for domain logic that leverages the global `Result` wrapper to eliminate redundant state classes.

### Modified Capabilities
- `clear-architecture`: Updating the UseCase requirement to use `Result<T>` as the standard output wrapper instead of per-use-case sealed classes.

## Impact

- **Domain Layer**: `UseCase.kt` and all use-case interfaces/implementations will be modified.
- **Presentation Layer**: All ViewModels calling these UseCases will need updates to their `when` blocks to handle `Result` instead of specific `Output` types.
- **DI**: No changes expected to Koin modules.
