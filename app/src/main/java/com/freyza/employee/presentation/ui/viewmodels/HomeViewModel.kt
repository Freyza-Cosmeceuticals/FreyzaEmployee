package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Constants
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val getRecentExpensesUseCase: GetRecentExpensesUseCase,
    private val getCurrentTravelPlanUseCase: GetCurrentTravelPlanUseCase,
    private val getTodayTravelPlanEntryUseCase: GetTodayTravelPlanEntryUseCase,
    private val getRouteUseCase: GetRouteUseCase,
    private val getLocationUseCase: GetLocationUseCase
) : ViewModel() {

    companion object {
        const val TAG = "HOME_VIEWMODEL"
    }

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState
        .onStart {
            val employeeId = sessionManager.currentEmployee.value?.id
            if (employeeId != null) loadCurrentTravelPlan(employeeId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeScreenUiState())

    fun loadRecentExpenses() {
        viewModelScope.launch {
            val result =
                getRecentExpensesUseCase.execute(GetRecentExpensesUseCase.Input(Constants.NUM_RECENT_EXPENSES))

            when (result) {
                is GetRecentExpensesUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(recentExpenses = UIState.Ready(result.expenses))
                    }
                    Logger.d(TAG, "${result.expenses.size} Recent Expenses Fetched Successfully")
                }

                is GetRecentExpensesUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(recentExpenses = UIState.Error(message = "Unable to fetch recent expenses"))
                    }
                    Logger.e(TAG, "Cannot fetch recent expenses: ${result.message}")
                }
            }
        }
    }

    fun loadCurrentTravelPlan(employeeId: String) {
        Logger.i(TAG, "Fetching current travel plan for $employeeId")
        viewModelScope.launch {
            _uiState.update { it.copy(currentTravelPlan = UIState.Loading(it.currentTravelPlan.data)) }
            when (val result = getCurrentTravelPlanUseCase.execute(
                GetCurrentTravelPlanUseCase.Input(
                    employeeId
                )
            )) {
                is GetCurrentTravelPlanUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(currentTravelPlan = UIState.Ready(result.travelPlan))
                    }
                    Logger.d(
                        TAG, "${result.travelPlan?.id} Current Travel Plan Fetched Successfully"
                    )

                    // fetch today's entry
                    if (result.travelPlan?.id !== null) loadTodayTravelPlanEntry(result.travelPlan.id)
                }

                is GetCurrentTravelPlanUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            currentTravelPlan = UIState.Error(message = "Unable to fetch current travel plan")
                        )
                    }
                    Logger.e(TAG, "Cannot fetch current travel plan: ${result.message}")
                }
            }
        }
    }

    private fun loadTodayTravelPlanEntry(tpId: String) {
        Logger.i(TAG, "Fetching today travel plan entry for tp:$tpId")
        viewModelScope.launch {
            _uiState.update { it.copy(todayTravelPlanEntry = UIState.Loading(it.todayTravelPlanEntry.data)) }

            when (val result = getTodayTravelPlanEntryUseCase.execute(
                GetTodayTravelPlanEntryUseCase.Input(tpId)
            )) {
                is GetTodayTravelPlanEntryUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(todayTravelPlanEntry = UIState.Ready(result.travelPlanEntry))
                    }
                    Logger.d(
                        TAG,
                        "${result.travelPlanEntry?.id} Today Travel Plan Entry Fetched Successfully"
                    )

                    // fetch plan's route
                    if (result.travelPlanEntry?.routeId !== null) loadCurrentRoute(result.travelPlanEntry.routeId)
                }

                is GetTodayTravelPlanEntryUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            todayTravelPlanEntry = UIState.Error(message = "Unable to fetch today travel plan entry")
                        )
                    }
                    Logger.e(TAG, "Cannot fetch today travel plan entry: ${result.message}")
                }
            }
        }
    }

    private fun loadCurrentRoute(routeId: String) {
        Logger.i(TAG, "Fetching route with $routeId")
        viewModelScope.launch {
            _uiState.update { it.copy(todayPlanEntryRoute = UIState.Loading(it.todayPlanEntryRoute.data)) }
            when (val result = getRouteUseCase.execute(GetRouteUseCase.Input(routeId))) {
                is GetRouteUseCase.Output.Success -> {
                    _uiState.update {
                        it.copy(todayPlanEntryRoute = UIState.Ready(result.route))
                    }
                    Logger.d(
                        TAG,
                        "${result.route?.id} Current Route Fetched Successfully ${result.route}"
                    )

                    // fetch both locations if route exists
                    if (result.route !== null) {
                        loadLocationPair(result.route.srcLocId, result.route.destLocId)
                    }
                }

                is GetRouteUseCase.Output.Failure -> {
                    _uiState.update {
                        it.copy(
                            todayPlanEntryRoute = UIState.Error(message = "Unable to fetch the route")
                        )
                    }
                    Logger.e(TAG, "Cannot fetch current route: ${result.message}")
                }
            }
        }
    }

    private fun loadLocationPair(srcLocId: String, destLocId: String) {
        Logger.i(TAG, "Fetching location pair for $srcLocId -> $destLocId")
        viewModelScope.launch {
            _uiState.update { it.copy(todayPlanEntrySrcDest = UIState.Loading(it.todayPlanEntrySrcDest.data)) }

            coroutineScope {
                val srcDeferred = async(Dispatchers.IO) {
                    getLocationUseCase.execute(GetLocationUseCase.Input(srcLocId))
                }

                val destDeferred = async(Dispatchers.IO) {
                    getLocationUseCase.execute(GetLocationUseCase.Input(destLocId))
                }

                val srcResult = srcDeferred.await()
                val destResult = destDeferred.await()

                if (srcResult is GetLocationUseCase.Output.Success && destResult is GetLocationUseCase.Output.Success) {
                    _uiState.update {
                        it.copy(
                            todayPlanEntrySrcDest = UIState.Ready(
                                if (srcResult.location != null && destResult.location != null) srcResult.location to destResult.location
                                else null
                            )
                        )
                    }

                    Logger.d(
                        TAG,
                        "${srcResult.location?.id} -> ${destResult.location?.id} Location Pair Fetched Successfully ${srcResult.location} -> ${destResult.location}"
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            todayPlanEntrySrcDest = UIState.Error(
                                message = "Unable to fetch locations"
                            )
                        )
                    }

                    if (srcResult is GetLocationUseCase.Output.Failure)
                        Logger.e(TAG, "Cannot fetch location pair: ${srcResult.message}")
                    if (destResult is GetLocationUseCase.Output.Failure)
                        Logger.e(TAG, "Cannot fetch location pair: ${destResult.message}")
                }
            }
        }
    }
}
