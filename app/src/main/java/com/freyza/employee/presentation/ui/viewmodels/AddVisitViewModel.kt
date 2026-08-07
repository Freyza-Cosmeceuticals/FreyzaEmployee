package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.LocationTracker
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.data.mappers.toDto
import com.freyza.employee.data.mappers.toFormState
import com.freyza.employee.data.mappers.toUpdateDto
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitParams
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import com.freyza.employee.presentation.ui.state.ProductEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddVisitViewModel(
  val visitType: VisitType,
  val reportId: String,
  val employeeId: String,
  val visitId: String? = null,
  private val sessionManager: SessionManager,
  private val createVisitUseCase: CreateVisitUseCase,
  private val dailyReportRepository: DailyReportRepository,
  private val routeRepository: RouteRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
  private val locationTracker: LocationTracker,
) : ViewModel() {
  companion object {
    const val TAG = "AddVisitViewModel"
  }

  val isEditMode = visitId != null

  private val _uiState =
    MutableStateFlow(
      AddVisitUiState(
        today = serverTime.nowLocalDateTime(),
        visitType = visitType,
        creationState = UIState.Idle(true)
      )
    )
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    AddVisitUiState(today = serverTime.nowLocalDateTime(), visitType = visitType)
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init with visitType: ${visitType.name}")
  }

  fun refresh() {
    loadAvailablePois()

    if (isEditMode) {
      loadVisit()
    } else {
      refreshLocation()
    }
  }

  private fun loadAvailablePois() {
    _uiState.update { it.copy(availablePois = UIState.Loading()) }
    viewModelScope.launch {
      // 1. Get Daily Report to find routeId
      val reportResult = dailyReportRepository.getDailyReport(reportId)
      if (reportResult is Result.Success) {
        val routeId = reportResult.data?.routeId
        if (routeId != null) {
          // 2. Get Route to find destLocId
          val routeResult = routeRepository.getRoute(routeId)
          if (routeResult is Result.Success) {
            val destLocId = routeResult.data?.destLocId
            if (destLocId != null) {
              // 3. Fetch POIs for this location and visit type
              val poisResult = dailyReportRepository.getPois(destLocId, visitType)
              if (poisResult is Result.Success) {
                _uiState.update { it.copy(availablePois = UIState.Ready(poisResult.data)) }
                return@launch
              }
            }
          }
        }
      }
      _uiState.update { it.copy(availablePois = UIState.Ready(emptyList())) }
    }
  }

  private fun loadVisit() {
    if (visitId == null) return

    _uiState.update {
      it.copy(creationState = UIState.Loading(null, message = "Loading visit details"))
    }

    viewModelScope.launch {
      when (val result = dailyReportRepository.getVisit(visitId)) {
        is Result.Success -> {
          val visit = result.data
          if (visit != null) {
            _uiState.update {
              it.copy(
                form = visit.toFormState(),
                creationState = UIState.Idle(true)
              )
            }
          } else {
            _uiState.update {
              it.copy(creationState = UIState.Error("Visit not found", true))
            }
          }
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(creationState = UIState.Error(result.message, true))
          }
        }

        else -> {}
      }
    }
  }

  fun refreshLocation() {
    Logger.d(TAG, "Refreshing location")
    viewModelScope.launch {
      locationTracker.getCurrentLocation()
    }
  }

  fun updateName(name: String) {
    _uiState.update { state ->
      val pois = state.availablePois.data ?: emptyList()
      val matchedPoi = pois.find {
        it.name.trim().equals(name.trim(), ignoreCase = true)
      }
      state.copy(
        form = state.form.copy(
          name = state.form.name.copy(value = name, error = null),
          poiId = matchedPoi?.id
        )
      )
    }
  }

  fun selectPoi(poi: PointOfInterest?) {
    _uiState.update { state ->
      state.copy(
        form = state.form.copy(
          name = state.form.name.copy(value = poi?.name ?: "", error = null),
          poiId = poi?.id
        )
      )
    }
  }

  fun updateNotes(notes: String) {
    _uiState.update {
      it.copy(form = it.form.copy(notes = it.form.notes.copy(value = notes, error = null)))
    }
  }

  fun updateSamplesGiven(samples: List<String>) {
    _uiState.update {
      it.copy(form = it.form.copy(samplesGiven = samples))
    }
  }

  fun updateOrderTaken(orderTaken: Boolean) {
    _uiState.update { state ->
      var newForm = state.form.copy(orderTaken = orderTaken)
      if (visitType == VisitType.DOCTOR && orderTaken && newForm.productEntries.isEmpty()) {
        newForm = newForm.copy(productEntries = listOf(ProductEntry()))
      } else if (!orderTaken) {
        newForm = newForm.copy(productEntries = emptyList())
      }
      state.copy(form = newForm)
    }
  }

  fun addProductEntry() {
    _uiState.update { state ->
      if (state.form.productEntries.size < Constants.MAX_PRODUCT_ENTRIES) {
        state.copy(form = state.form.copy(productEntries = state.form.productEntries + ProductEntry()))
      } else state
    }
  }

  fun removeProductEntry(index: Int) {
    _uiState.update { state ->
      val newList = state.form.productEntries.toMutableList().apply { removeAt(index) }
      state.copy(form = state.form.copy(productEntries = newList))
    }
  }

  fun updateProductEntry(index: Int, entry: ProductEntry) {
    _uiState.update { state ->
      val newList = state.form.productEntries.toMutableList().apply { set(index, entry) }
      state.copy(form = state.form.copy(productEntries = newList))
    }
  }

  fun updateOutstandingAmount(amount: String) {
    _uiState.update {
      it.copy(
        form = it.form.copy(
          outstandingAmount = it.form.outstandingAmount.copy(
            value = amount,
            error = null
          )
        )
      )
    }
  }

  fun updateBillNo(billNo: String) {
    _uiState.update {
      it.copy(form = it.form.copy(billNo = it.form.billNo.copy(value = billNo, error = null)))
    }
  }

  fun updatePaymentCollected(collected: Boolean) {
    _uiState.update {
      it.copy(form = it.form.copy(paymentCollected = collected))
    }
  }

  fun updateAmountWithGST(amount: String) {
    _uiState.update {
      it.copy(
        form = it.form.copy(
          amountWithGST = it.form.amountWithGST.copy(
            value = amount,
            error = null
          )
        )
      )
    }
  }

  fun updateAmountWithoutGST(amount: String) {
    _uiState.update {
      it.copy(
        form = it.form.copy(
          amountWithoutGST = it.form.amountWithoutGST.copy(
            value = amount,
            error = null
          )
        )
      )
    }
  }

  fun updateStockChecked(checked: Boolean) {
    _uiState.update {
      it.copy(form = it.form.copy(stockChecked = checked))
    }
  }

  private fun validateForm(): Boolean {
    val form = _uiState.value.form
    var isValid = true

    val newName = if (form.name.value.isBlank()) {
      isValid = false
      form.name.copy(error = "Name cannot be empty")
    } else form.name

    val newProductEntries = if (form.orderTaken) {
      form.productEntries.map { entry ->
        val validatedEntry = entry.validate()
        if (!validatedEntry.isValid) isValid = false
        validatedEntry
      }
    } else form.productEntries

    val (newAmountWithGST, newAmountWithoutGST) = if (form.paymentCollected) {
      var retVal =
        Pair(form.amountWithGST.copy(error = null), form.amountWithoutGST.copy(error = null))

      if (!form.amountWithGST.isValidNumber) {
        isValid = false
        retVal = retVal.copy(first = form.amountWithGST.copy(error = "Invalid"))
      }

      if (!form.amountWithoutGST.isValidNumber) {
        isValid = false
        retVal = retVal.copy(second = form.amountWithoutGST.copy(error = "Invalid"))
      }
      retVal

    } else Pair(form.amountWithGST.copy(error = null), form.amountWithoutGST.copy(error = null))

    _uiState.update {
      it.copy(
        form = form.copy(
          name = newName,
          productEntries = newProductEntries,
          amountWithGST = newAmountWithGST,
          amountWithoutGST = newAmountWithoutGST
        )
      )
    }

    return isValid
  }

  fun submitVisit() {
    if (!validateForm()) {
      snackbarManager.showError("Please fix errors in the form")
      return
    }
    val currentState = _uiState.value
    val form = currentState.form
    val visitType = currentState.visitType

    // guard visitType
    if (visitType == null) {
      _uiState.update {
        it.copy(creationState = UIState.Error("Invalid visit type selected", null))
      }

      Logger.e(TAG, "IMPOSSIBLE: visitType null when submitting a visit")
      return
    }

    // guard employeeId
    val employeeId = sessionManager.currentEmployee.value?.id
    Logger.d(TAG, "${if (isEditMode) "Updating" else "Creating"} visit for emp:$employeeId")
    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set, cannot create visit, aborting")
      return
    }

    _uiState.update {
      it.copy(creationState = UIState.Loading(null, message = "Marking visit"))
    }
    Logger.d(TAG, "Submitting visit with data $form")

    viewModelScope.launch {
      if (isEditMode && visitId != null) {
        val updateDto = form.toUpdateDto(visitType, updatedAt = serverTime.now().toString())
        when (val result = dailyReportRepository.updateVisit(visitId, updateDto)) {
          is Result.Success -> {
            _uiState.update {
              it.copy(creationState = UIState.Ready(false))
            }
            snackbarManager.showSuccess("Visit updated successfully")
            Logger.i(TAG, "visit:${result.data.id} Visit updated successfully.")
          }

          is Result.Error -> {
            _uiState.update {
              it.copy(
                creationState = UIState.Error(
                  "Unable to update visit, please try again",
                  true
                )
              )
            }
            if (result.message.isNotEmpty()) {
              snackbarManager.showError("Unable to update visit. ${result.message}")
            } else {
              snackbarManager.showError("Unable to update visit, please try again")
            }
            Logger.e(TAG, "Cannot update visit: ${result.message}")
          }

          else -> {}
        }
      } else {
        val coords = locationTracker.getCurrentLocation()
        Logger.d(TAG, "Got location from tracker, $coords")

        val dto = form.toDto(
          reportId = reportId,
          employeeId = employeeId,
          visitType = visitType,
          latitude = coords?.latitude ?: 0.0,
          longitude = coords?.longitude ?: 0.0
        )

        val result = createVisitUseCase(
          CreateVisitParams(
            today = serverTime.todayIn(),
            employeeId = employeeId,
            dailyReportId = reportId,
            visitCreateDto = dto
          )
        )

        when (result) {
          is Result.Success -> {
            _uiState.update {
              // canSubmit = false
              it.copy(creationState = UIState.Ready(false))
            }
            snackbarManager.showSuccess("Visit created successfully")
            Logger.i(
              TAG, "visit:${result.data.id} New visit marked successfully."
            )
          }

          is Result.Error -> {
            _uiState.update {
              it.copy(creationState = UIState.Error("Unable to mark visit, please try again", true))
            }

            if (result.message.isNotEmpty()) {
              snackbarManager.showError("Unable to mark visit. ${result.message}")
            } else {
              snackbarManager.showError("Unable to mark visit, please try again")
            }
            Logger.e(TAG, "Cannot mark visit: ${result.message}")
          }

          else -> {}
        }
      }
    }
  }
}
