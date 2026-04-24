package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.data.network.dto.VisitCreateDto
import com.freyza.employee.domain.model.VisitCreate
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import com.freyza.employee.presentation.ui.state.AddVisitUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class AddVisitViewModel(
  val visitType: VisitType,
  val reportId: String,
  val employeeId: String,
  private val sessionManager: SessionManager,
  private val createVisitUseCase: CreateVisitUseCase,
) :
  ViewModel() {
  companion object {
    const val TAG = "AddVisitViewModel"
  }

  private val _uiState = MutableStateFlow(AddVisitUiState(visitType = visitType))
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5_000), AddVisitUiState(visitType = visitType)
  )

  init {
    Logger.d(TAG, "Init with visitType: ${visitType.name}")
  }

  fun refresh() {
//    Logger.d(TAG, "Refreshing data")
//    val employeeId = sessionManager.currentEmployee.value?.id
  }

  fun submitVisit(visitCreate: VisitCreate) {
    val visitType = _uiState.value.visitType
    if (visitType == null) {
      _uiState.update {
        it.copy(creationState = UIState.Error("Invalid visit type selected"))
      }

      Logger.e(TAG, "IMPOSSIBLE: visitType null when submitting a visit")
      return
    }

    val employeeId = sessionManager.currentEmployee.value?.id
    Logger.i(TAG, "Creating visit for emp:$employeeId")
    if (employeeId == null) {
      Logger.e(TAG, "Current employee not set, cannot create visit, aborting")
      return
    }

    Logger.d(TAG, "Submitting visit with data $visitCreate")

    _uiState.update {
      it.copy(creationState = UIState.Loading(null, message = "Marking visit"))
    }

    viewModelScope.launch {
      val dto = VisitCreateDto(
        reportId = reportId,
        employeeId = employeeId,
        visitType = visitType,
        latitude = 96.00,
        longitude = 45.00,
        distanceMetersFromPOI = 500,
        doctorName = visitCreate.doctorName,
        chemistName = visitCreate.chemistName,
        stockistName = visitCreate.stockistName,
        productDetails = visitCreate.productDetails,
        samplesGiven = visitCreate.samplesGiven,
        orderTaken = visitCreate.orderTaken,
        billNo = visitCreate.billNo,
        paymentCollected = visitCreate.paymentCollected,
        amountWithGST = visitCreate.amountWithGST,
        amountWithoutGST = visitCreate.amountWithoutGST,
        outstandingAmount = visitCreate.outstandingAmount,
        orderAmount = visitCreate.orderAmount,
        stockChecked = visitCreate.stockChecked,
        additionalNotes = visitCreate.notes
      )

      when (val result = createVisitUseCase.execute(
        CreateVisitUseCase.Input(
          today = Clock.System.todayIn(TimeZone.of(Constants.TIMEZONE)),
          employeeId = employeeId,
          dailyReportId = reportId,
          visitCreateDto = dto
        )
      )) {
        is CreateVisitUseCase.Output.Success -> {
          _uiState.update {
            it.copy(creationState = UIState.Ready(Unit))
          }
          Logger.i(
            TAG, "visit:${result.visit?.id} New visit marked successfully."
          )
        }

        is CreateVisitUseCase.Output.Failure -> {
          _uiState.update {
            it.copy(creationState = UIState.Error("Unable to mark visit, please try again"))
          }
          Logger.e(TAG, "Cannot mark visit: ${result.message}")
        }
      }
    }
  }
}
