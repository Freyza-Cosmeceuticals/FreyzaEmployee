package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.presentation.ui.state.TravelPlanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TravelPlanViewModel(
  private val sessionManager: SessionManager,
  private val routeRepository: RouteRepository,
  private val travelPlanRepository: TravelPlanRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "TravelPlanViewModel"
  }

  private val _uiState = MutableStateFlow(TravelPlanUiState(today = serverTime.nowLocalDateTime()))
  val uiState = _uiState.onStart {
    refresh()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    TravelPlanUiState(today = serverTime.nowLocalDateTime())
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
    observeServerTime()
  }

  private fun observeServerTime() {
    viewModelScope.launch {
      serverTime.isSynced.collect { synced ->
        if (synced) {
          val nowTime = serverTime.nowLocalDateTime()
          _uiState.update { it.copy(today = nowTime) }
        }
      }
    }
  }

  fun refresh(forceRefresh: Boolean = false) {
    loadAllRoutes(forceRefresh)

    val employeeId = sessionManager.currentEmployee.value?.id
    if (employeeId != null) loadCurrentTravelPlan(employeeId, forceRefresh)
  }

  fun loadCurrentTravelPlan(employeeId: String, forceRefresh: Boolean = false) {
    Logger.d(TAG, "Fetching current travel plan for $employeeId")

    _uiState.update {
      it.copy(
        currentTravelPlan = UIState.Loading(it.currentTravelPlan.data)
      )
    }
    viewModelScope.launch {
      when (val result =
        travelPlanRepository.getCurrentTravelPlan(employeeId, forceRefresh = forceRefresh)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(currentTravelPlan = UIState.Ready(result.data))
          }
//          snackbarManager.showSuccess("Travel Plan Fetched Successfully")
          Logger.d(
            TAG, "${result.data?.id} Current Travel Plan Fetched Successfully"
          )

          // fetch all entries
          if (result.data?.id !== null) loadTravelPlanEntries(result.data.id, forceRefresh)
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(
              currentTravelPlan = UIState.Error(message = "Unable to fetch current travel plan")
            )
          }
          Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
        }

        else -> {}
      }
    }
  }

  private fun loadAllRoutes(forceRefresh: Boolean = false) {
    Logger.d(TAG, "Fetching all routes")

    viewModelScope.launch {
      when (val result = routeRepository.getAllRoutesWithLocation(forceRefresh = forceRefresh)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(routes = result.data)
          }
          Logger.d(
            TAG, "All routes fetched successfully: ${result.data.size} routes"
          )
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(
              routes = it.routes
            )
          }
          Logger.e(TAG, "Cannot fetch all routes with location: ${result.message}")
        }

        else -> {}
      }
    }
  }

  private fun loadTravelPlanEntries(tpId: String, forceRefresh: Boolean = false) {
    Logger.d(TAG, "Fetching plan Entries plan for tpId:$tpId")

    _uiState.update {
      it.copy(
        travelPlanEntries = UIState.Loading(it.travelPlanEntries.data)
      )
    }

    viewModelScope.launch {
      when (val result =
        travelPlanRepository.getTravelPlanEntries(tpId, forceRefresh = forceRefresh)) {
        is Result.Success -> {
          _uiState.update {
            it.copy(travelPlanEntries = UIState.Ready(result.data))
          }

          Logger.d(
            TAG, "${result.data.size} Travel Plans Fetched Successfully"
          )
        }

        is Result.Error -> {
          _uiState.update {
            it.copy(
              travelPlanEntries = UIState.Error(message = "Unable to fetch travel plans")
            )
          }
          Logger.e(TAG, "Cannot fetch travel plans: ${result.message}")
        }

        else -> {}
      }
    }
  }

  fun loadSelectedPlanEntryRoute(tpEntryId: String) {
    Logger.d(TAG, "Fetching plan entry route data for tpEntryId:$tpEntryId")

    _uiState.update {
      it.copy(
        selectedRoute = UIState.Loading(it.selectedRoute.data)
      )
    }

    viewModelScope.launch {
      _uiState.mapNotNull { it.routes }.first { it.isNotEmpty() }
        .let { availableRoutes ->
          val thisTravelPlan = _uiState.value.travelPlanEntries.data?.find { it.id == tpEntryId }
          val thisRoute = availableRoutes.find { it.id == thisTravelPlan?.routeId }

          if (thisRoute != null) {
            _uiState.update {
              it.copy(selectedRoute = UIState.Ready(thisRoute))
            }

            Logger.d(
              TAG, "${thisRoute.id} route set successfully"
            )
          } else {
            _uiState.update {
              it.copy(
                selectedRoute = UIState.Error("Route not found for plan entry")
              )
            }
          }
        }
    }
  }
}
