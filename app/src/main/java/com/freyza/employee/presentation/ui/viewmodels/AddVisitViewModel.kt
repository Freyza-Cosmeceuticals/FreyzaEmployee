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
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitParams
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import com.freyza.employee.presentation.ui.state.AddVisitFormState
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
  private val sessionManager: SessionManager,
  private val createVisitUseCase: CreateVisitUseCase,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
  private val locationTracker: LocationTracker,
) : ViewModel() {
  companion object {
    const val TAG = "AddVisitViewModel"
  }

  private val _uiState =
    MutableStateFlow(AddVisitUiState(today = serverTime.nowLocalDateTime(), visitType = visitType))
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
    Logger.d(TAG, "Refreshing location")
    viewModelScope.launch {
      locationTracker.getCurrentLocation()
    }
  }

  // TODO: Better form validation and feedback
  fun updateForm(updatedData: AddVisitFormState) {
    var newData = updatedData
    val oldData = _uiState.value.form

    if (newData.orderTaken) {
      if (!oldData.orderTaken) {
        // if just toggled orderTaken, add atleast one product entry
        newData = newData.copy(productEntries = listOf(ProductEntry()))
      }

      // if emptied, ensure atleast one is present while order taken is true
      if (newData.productEntries.isEmpty()) {
        newData = newData.copy(productEntries = listOf(ProductEntry()))
      }

      if (newData.productEntries.size > Constants.MAX_PRODUCT_ENTRIES) {
        newData =
          newData.copy(productEntries = newData.productEntries.take(Constants.MAX_PRODUCT_ENTRIES))
      }
    } else {
      // if off, product entries must be empty
      if (newData.productEntries.isNotEmpty()) {
        newData = newData.copy(productEntries = listOf())
      }
    }

    val nameValid =
      newData.doctorName.isNotBlank() || newData.chemistName.isNotBlank() || newData.stockistName.isNotBlank()

    _uiState.update { it.copy(form = newData, creationState = UIState.Idle(nameValid)) }

  }

  fun submitVisit() {
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
    Logger.i(TAG, "Creating visit for emp:$employeeId")
    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set, cannot create visit, aborting")
      return
    }

    _uiState.update {
      it.copy(creationState = UIState.Loading(null, message = "Marking visit"))
    }
    Logger.d(TAG, "Submitting visit with data $form")

    viewModelScope.launch {
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
          snackbarManager.showError("Unable to mark visit, please try again")
          Logger.e(TAG, "Cannot mark visit: ${result.message}")
        }

        else -> {}
      }
    }
  }
}
