package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.toRouteWithLocation
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportParams
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportParams
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
  private val sessionManager: SessionManager,
  private val getCurrentTravelPlanUseCase: GetCurrentTravelPlanUseCase,
  private val getTodayTravelPlanEntryUseCase: GetTodayTravelPlanEntryUseCase,
  private val getRouteUseCase: GetRouteUseCase,
  private val getLocationUseCase: GetLocationUseCase,
  private val getAllRoutesWithLocationUseCase: GetAllRoutesWithLocationUseCase,
  private val getTodayDailyReportUseCase: GetTodayDailyReportUseCase,
  private val createTodayDailyReportUseCase: CreateTodayDailyReportUseCase,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
) : ViewModel() {

  companion object {
    const val TAG = "HomeViewModel"
  }

  private val _uiState = MutableStateFlow(HomeScreenUiState(today = serverTime.nowLocalDateTime()))
  val uiState = _uiState.onStart {
    initializeData()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5_000),
    HomeScreenUiState(today = serverTime.nowLocalDateTime())
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
  }

  private fun initializeData() {
    val user = sessionManager.currentEmployee.value
    _uiState.update { it.copy(greetingName = user?.name ?: "") }
    refresh()
  }

  fun refresh() {
    Logger.d(TAG, "Refreshing data")
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

      // Execute independent fetches concurrently
      val reportJob = launch { loadCurrentDailyReport(employeeId) }
      val routesJob = launch { loadAllRoutes() }
      val planJob = launch { loadCurrentTravelPlan(employeeId) }

      reportJob.join()
      routesJob.join()
      planJob.join()

      _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
    }
  }

  private suspend fun loadCurrentTravelPlan(employeeId: String) {
    when (val result = getCurrentTravelPlanUseCase(employeeId)) {
      is Result.Success -> {
        _uiState.update { it.copy(currentTravelPlan = result.data) }
        result.data?.id?.let { loadTodayTravelPlanEntry(it) }
      }

      is Result.Error -> {
        _uiState.update { it.copy(errorMessage = "Unable to fetch travel plan") }
        Logger.e(TAG, "Fetch travel plan failed: ${result.message}")
      }

      else -> {}
    }
  }

  private suspend fun loadTodayTravelPlanEntry(tpId: String) {
    when (val result = getTodayTravelPlanEntryUseCase(tpId)) {
      is Result.Success -> {
        _uiState.update { it.copy(todayTravelPlanEntry = result.data) }
        result.data?.routeId?.let { loadCurrentRoute(it) }
      }

      is Result.Error -> {
        Logger.e(TAG, "Fetch travel plan entry failed: ${result.message}")
      }

      else -> {}
    }
  }

  private suspend fun loadCurrentRoute(routeId: String) {
    when (val result = getRouteUseCase(routeId)) {
      is Result.Success -> {
        if (result.data != null) {
          loadLocations(result.data)
        }
      }

      is Result.Error -> Logger.e(TAG, "Fetch route failed: ${result.message}")
      else -> {}
    }
  }

  private suspend fun loadLocations(route: Route) {
    coroutineScope {
      val srcDeferred = async(Dispatchers.IO) { getLocationUseCase(route.srcLocId) }
      val destDeferred = async(Dispatchers.IO) { getLocationUseCase(route.destLocId) }

      val srcResult = srcDeferred.await()
      val destResult = destDeferred.await()

      if (srcResult is Result.Success && destResult is Result.Success) {
        if (srcResult.data != null && destResult.data != null) {

          _uiState.update { state ->
            state.copy(
              todayPlanEntryRoute = route.toRouteWithLocation(
                srcLoc = srcResult.data, destLoc = destResult.data
              )
            )
          }
        }
      } else {
        Logger.e(TAG, "Failed to fetch location pair")
      }
    }
  }

  private suspend fun loadCurrentDailyReport(employeeId: String) {
    val result = getTodayDailyReportUseCase(
      GetTodayDailyReportParams(serverTime.todayIn(), employeeId, true)
    )

    when (result) {
      is Result.Success -> {
        val report = result.data
        _uiState.update {
          it.copy(
            currentDailyReport = report,
            showCreateReportSheet = report == null,
            todayReportDayType = report?.dayType,
            todayReportRoute = null
          )
        }

        report?.routeId?.let { setReportRoute(it) }
      }

      is Result.Error -> {
        _uiState.update { it.copy(errorMessage = "Unable to fetch daily report") }
        Logger.e(TAG, "Fetch daily report failed: ${result.message}")
      }

      else -> {}
    }
  }

  private fun setReportRoute(routeId: String?) {
    if (routeId == null) return

    viewModelScope.launch {
      _uiState.map { it.routes }.first { it.isNotEmpty() }.let { routes ->
        val route = routes.find { it.id == routeId }
        _uiState.update { it.copy(todayReportRoute = route) }
      }
    }
  }

  fun createCurrentDailyReport(dayType: DayType, routeId: String?) {
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
      val result = createTodayDailyReportUseCase(
        CreateTodayDailyReportParams(serverTime.todayIn(), employeeId, dayType, routeId)
      )

      when (result) {
        is Result.Success -> {
          _uiState.update {
            it.copy(
              isLoading = false,
              currentDailyReport = result.data,
              showCreateReportSheet = false,
              todayReportDayType = result.data.dayType
            )
          }
          setReportRoute(result.data.routeId)
          snackbarManager.showSuccess("Daily report created successfully")
        }

        is Result.Error -> {
          _uiState.update { it.copy(isLoading = false) }
          snackbarManager.showError("Unable to create daily report, please try again")
        }

        else -> {}
      }
    }
  }

  private suspend fun loadAllRoutes() {
    when (val result = getAllRoutesWithLocationUseCase()) {
      is Result.Success -> _uiState.update { it.copy(routes = result.data) }
      is Result.Error -> Logger.e(TAG, "Fetch routes failed: ${result.message}")
      else -> {}
    }
  }

  fun dismissCreateReportSheet() {
    _uiState.update { it.copy(showCreateReportSheet = false) }
  }
}
