# Visits Specification

## Overview
Visit creation screen for adding doctor, chemist, or stockist visits to daily reports.

## ViewModel

### AddVisitViewModel
- **File**: `presentation/ui/viewmodels/AddVisitViewModel.kt`
- **Tag**: `"AddVisitViewModel"`
- **Injected via**: `parametersOf(visitType, reportId, employeeId)`

## State

### AddVisitUiState
- **File**: `presentation/ui/state/AddVisitUiState.kt`

```kotlin
data class AddVisitUiState(
  val visitType: VisitType?,
  val creationState: UIState<Unit> = UIState.Idle(),
)
```

## UseCase

### CreateVisitUseCase
- **Input**: today, employeeId, dailyReportId, visitCreateDto
- **Output**: Success(visit) | Failure(message)

## Visit Types

### VisitType Enum
```kotlin
enum class VisitType {
  DOCTOR,
  STOCKIST,
  CHEMIST;
}
```

## Visit Models

### Visit (Sealed Class)
- `DoctorVisit` - doctorName, productDetails, samplesGiven, orderTaken, orderAmount, outstandingAmount
- `ChemistVisit` - chemistName, orderTaken, outstandingAmount
- `StockistVisit` - stockistName, billNo, paymentCollected, amountWithGST, amountWithoutGST, stockChecked

### VisitCreate
```kotlin
data class VisitCreate(
  val doctorName: String?,
  val stockistName: String?,
  val chemistName: String?,
  val productDetails: List<ProductDetail>,
  val samplesGiven: List<String>,
  val orderTaken: Boolean,
  val billNo: String?,
  val paymentCollected: Boolean,
  val amountWithGST: Double,
  val amountWithoutGST: Double,
  val outstandingAmount: Double,
  val orderAmount: Double?,
  val stockChecked: Boolean,
  val notes: String?,
)
```

### ProductDetail
```kotlin
data class ProductDetail(
  val name: String,
  val rate: Double,
  val quantity: Int
)
```

## Functions

### submitVisit(visitCreate)
- Creates visit with DTO conversion
- Uses hardcoded lat/lng (96.00, 45.00) and distance (500m) - TODO: real GPS

## Route

- `NavRoutes.Authenticated.AddVisit(type, reportId, employeeId)`

## DI Registration

```kotlin
viewModel { (visitType: VisitType, reportId: String, employeeId: String) ->
  AddVisitViewModel(visitType, reportId, employeeId, get(), get(), get())
}
```