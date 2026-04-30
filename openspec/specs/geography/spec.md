# Geography Specification

## Overview
Location and Route management for geographic data.

## Location

### Location Model
- **File**: `domain/model/Location.kt`

```kotlin
data class Location(
  val id: String,
  val name: String,
  val operational: Boolean,
  val createdAt: Instant,
  val updatedAt: Instant?,
)
```

### GetLocationUseCase
- **Interface**: `domain/usecase/location/GetLocationUseCase.kt`
- **Input**: locationId
- **Output**: Success(location) | Failure(message)
- **Impl**: `domain/usecase/location/impl/GetLocationUseCaseImpl.kt`

### LocationRepository
- **Interface**: `domain/repository/LocationRepository.kt`
- **Impl**: `data/repository/LocationRepositoryImpl.kt`
- **Methods**: `suspend fun getLocation(id): Result<Location>`

## Route

### Route Model
- **File**: `domain/model/Route.kt`

```kotlin
data class Route(
  val id: String,
  val srcLocId: String,
  val destLocId: String,
  val distanceKm: Double?,
)
```

### RouteWithLocation
```kotlin
data class RouteWithLocation(
  val route: Route,
  val srcLoc: Location,
  val destLoc: Location,
) {
  fun routeName(): String = "${srcLoc.name} → ${destLoc.name}"
}
```

### GetRouteUseCase
- **Input**: routeId
- **Output**: Success(route) | Failure(message)

### GetAllRoutesWithLocationUseCase
- **Output**: Success(routes) | Failure(message)

### RouteRepository
- **Interface**: `domain/repository/RouteRepository.kt`
- **Impl**: `data/repository/RouteRepositoryImpl.kt`
- **Methods**: 
  - `suspend fun getRoute(id): Result<Route>`
  - `suspend fun getAllRoutes(): Result<List<Route>>`

## HomeViewModel Usage

```kotlin
private fun loadLocationPair(srcLocId: String, destLocId: String)
```
- Fetches source and destination locations concurrently
- Combines into RouteWithLocation

## DI Registration

```kotlin
single<GetLocationUseCase> { GetLocationUseCaseImpl(get()) }
single<GetRouteUseCase> { GetRouteUseCaseImpl(get()) }
single<GetAllRoutesWithLocationUseCase> { GetAllRoutesWithLocationUseCaseImpl(get()) }
single<LocationRepository> { LocationRepositoryImpl(get()) }
single<RouteRepository> { RouteRepositoryImpl(get()) }
```