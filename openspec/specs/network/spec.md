# Network Specification

## Overview
DTOs for Supabase API communication.

## DTOs

### UserDto
- **File**: `data/network/dto/UserDto.kt`
- `@Serializable` annotated

### DailyReportDto
- **File**: `data/network/dto/DailyReportDto.kt`

```kotlin
@Serializable
data class DailyReportDto(
  val id: String,
  val employeeId: String,
  val date: String,
  val dayType: DayType,
  val routeId: String?,
  val ta: Double?,
  val da: Double?,
  val totalExpense: Double?,
  val visits: List<VisitDto>?,
  val locked: Boolean,
  val lockedAt: String?,
  val createdAt: String,
  val updatedAt: String?,
)

@Serializable
data class DailyReportCreateDto(
  val employeeId: String,
  val date: String,
  val dayType: DayType,
  val routeId: String?,
)
```

### TravelPlanDto
- **File**: `data/network/dto/TravelPlanDto.kt`

### TravelPlanEntryDto
- **File**: `data/network/dto/TravelPlanEntryDto.kt`

### RouteDto
- **File**: `data/network/dto/RouteDto.kt`

### LocationDto
- **File**: `data/network/dto/LocationDto.kt`

### VisitDto
- **File**: `data/network/dto/VisitDto.kt`

### VisitCreateDto
- **File**: `data/network/dto/VisitDto.kt`
- For creating new visits

## Serialization

- All DTOs use `@Serializable` annotation
- Uses `@SerialName` for field mapping
- kotlinx.serialization library

## Key Patterns

### Date Fields
- Stored as String in DTOs
- Parsed to kotlinx.datetime types in mappers

### Nested Objects
- `visits: List<VisitDto>?` - nullable list
- Uses `?.map { } ?: emptyList()` for null safety