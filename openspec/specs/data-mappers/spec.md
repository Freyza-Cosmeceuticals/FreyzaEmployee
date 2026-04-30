# Data Mappers Specification

## Overview
DTO to Domain model mappers.

## Mappers

### UserMapper
- **File**: `data/mappers/User.kt`
- `UserDto.toDomain(): User`

### DailyReportMapper
- **File**: `data/mappers/DailyReport.kt`
- `DailyReportDto.toDomain(): DailyReport`
- `DailyReport.toDto(): DailyReportDto`

### TravelPlanMapper
- **File**: `data/mappers/TravelPlan.kt`
- `TravelPlanDto.toDomain(): TravelPlan`

### RouteMapper
- **File**: `data/mappers/Route.kt`
- `RouteDto.toDomain(): Route`

### LocationMapper
- **File**: `data/mappers/Location.kt`
- `LocationDto.toDomain(): Location`

### VisitMapper
- **File**: `data/mappers/Visit.kt`
- `VisitDto.toDomain(): Visit`

## Mapping Pattern

```kotlin
fun DtoType.toDomain(): DomainType {
  return DomainType(
    field1 = field1,
    field2 = field2,
    ...
  )
}
```

## Common Conversions

### Date/Time
- `LocalDate.parse(dateString)` for dates
- `Instant.parse(timestampString)` for timestamps
- DTO stores as String, Domain uses kotlinx.datetime types

### Nested Collections
```kotlin
visits = visits?.map { it.toDomain() } ?: emptyList()
```

### Nullable Fields
```kotlin
lockedAt = lockedAt?.let { Instant.parse(it) }
updatedAt = updatedAt?.let { Instant.parse(it) }
```