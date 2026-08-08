package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportParams
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
  private val sessionManager: SessionManager,
  private val userRepository: UserRepository,
  private val locationRepository: LocationRepository,
  private val dailyReportRepository: DailyReportRepository,
  private val routeRepository: RouteRepository,
  private val travelPlanRepository: TravelPlanRepository,
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

  fun refresh(forceRefresh: Boolean = false) {
    Logger.d(TAG, "Refreshing data, forceRefresh:$forceRefresh")
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

      // fetch routes and locations first
      val routesJob = launch { loadAllRoutes(forceRefresh) }
      val locationsJob = launch { loadAllLocations(forceRefresh) }
      val employeesJob = launch { loadHqEmployees(employeeId, forceRefresh) }

      routesJob.join()
      locationsJob.join()
      employeesJob.join()

      val planJob = launch { loadCurrentTravelPlan(employeeId, forceRefresh) }
      val reportJob = launch { loadCurrentDailyReport(employeeId, forceRefresh) }

      planJob.join()
      reportJob.join()

      _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
    }
  }

  private suspend fun loadCurrentTravelPlan(employeeId: String, forceRefresh: Boolean) {
    when (val result =
      travelPlanRepository.getCurrentTravelPlan(employeeId, forceRefresh = forceRefresh)) {
      is Result.Success -> {
        _uiState.update { it.copy(currentTravelPlan = result.data) }
        result.data?.id?.let { loadTodayTravelPlanEntry(it, forceRefresh) }
      }

      is Result.Error -> {
        _uiState.update { it.copy(errorMessage = "Unable to fetch travel plan") }
        Logger.e(TAG, "Fetch travel plan failed: ${result.message}")
      }

      else -> {}
    }
  }

  private suspend fun loadTodayTravelPlanEntry(tpId: String, forceRefresh: Boolean) {
    when (val result =
      travelPlanRepository.getTodayTravelPlanEntry(tpId, forceRefresh = forceRefresh)) {
      is Result.Success -> {
        val entry = result.data
        val route = _uiState.value.routes.find { it.id == entry?.routeId }

        _uiState.update { it.copy(todayTravelPlanEntry = entry, todayPlanEntryRoute = route) }
      }

      is Result.Error -> {
        Logger.e(TAG, "Fetch travel plan entry failed: ${result.message}")
      }

      else -> {}
    }
  }

  private suspend fun loadCurrentDailyReport(employeeId: String, forceRefresh: Boolean) {
    val result = dailyReportRepository.getTodayDailyReport(
      today = serverTime.todayIn(),
      employeeId = employeeId,
      withVisits = true,
      forceRefresh = forceRefresh
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

        report?.routeId?.let { routeId ->
          setReportRoute(routeId)
          viewModelScope.launch { loadReportPois(routeId, forceRefresh) }
        }
      }

      is Result.Error -> {
        _uiState.update { it.copy(errorMessage = "Unable to fetch daily report") }
        Logger.e(TAG, "Fetch daily report failed: ${result.message}")
      }

      else -> {}
    }
  }

  private suspend fun loadReportPois(routeId: String, forceRefresh: Boolean) {
    val route = _uiState.value.routes.find { it.id == routeId }
    val destLocId = route?.destLoc?.id ?: return

    when (val result = dailyReportRepository.getPoisByLocation(destLocId, forceRefresh)) {
      is Result.Success -> {
        _uiState.update { it.copy(pois = result.data) }
      }

      is Result.Error -> Logger.e(TAG, "Fetch POIs failed: ${result.message}")
      else -> {}
    }
  }

  private fun setReportRoute(routeId: String?) {
    if (routeId == null) return
    val route = _uiState.value.routes.find { it.id == routeId }
    _uiState.update { it.copy(todayReportRoute = route) }
  }

  fun createCurrentDailyReport(
    dayType: DayType,
    srcLocId: String?,
    destLocId: String?,
    travellingWithId: String? = null,
  ) {
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
      var finalRouteId: String? = null

      if (dayType == DayType.WORK && srcLocId != null && destLocId != null) {
        val localRoute = _uiState.value.routes.find {
          it.srcLoc.id == srcLocId && it.destLoc.id == destLocId
        }

        if (localRoute != null) {
          finalRouteId = localRoute.id
        } else {
          // New route detected, call RPC
          when (val routeResult = routeRepository.getOrCreateRoute(srcLocId, destLocId)) {
            is Result.Success -> {
              val newRoute = routeResult.data
              finalRouteId = newRoute.id
              Logger.w(
                TAG,
                "New route created on-the-fly: ID=${newRoute.id}, " + "SrcLocId=$srcLocId, DestLocId=$destLocId. Manual tweak may be needed."
              )
              // Refresh routes list to include the newly created route
              loadAllRoutes()
            }

            is Result.Error -> {
              _uiState.update { it.copy(isLoading = false) }
              snackbarManager.showError("Unable to resolve route, please try again")
              return@launch
            }

            else -> {}
          }
        }
      }

      val result = createTodayDailyReportUseCase(
        CreateTodayDailyReportParams(
          serverTime.todayIn(), employeeId, dayType, finalRouteId, travellingWithId
        )
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

  private suspend fun loadAllRoutes(forceRefresh: Boolean = false) {
    when (val result = routeRepository.getAllRoutesWithLocation(forceRefresh = forceRefresh)) {
      is Result.Success -> _uiState.update { it.copy(routes = result.data) }
      is Result.Error -> Logger.e(TAG, "Fetch routes failed: ${result.message}")
      else -> {}
    }
  }

  private suspend fun loadAllLocations(forceRefresh: Boolean = false) {
    when (val result = locationRepository.getAllLocations(forceRefresh = forceRefresh)) {
      is Result.Success -> _uiState.update { it.copy(locations = result.data) }
      is Result.Error -> Logger.e(TAG, "Fetch locations failed: ${result.message}")
      else -> {}
    }
  }

  private suspend fun loadHqEmployees(employeeId: String, forceRefresh: Boolean = false) {
    val user = sessionManager.currentEmployee.value
    val hqId = user?.hqId ?: return

    when (val result = userRepository.getEmployeesByHq(hqId = hqId, forceRefresh = forceRefresh)) {
      is Result.Success -> {
        val employees = result.data.filter { it.id != employeeId }
        _uiState.update { it.copy(employees = employees) }
      }

      is Result.Error -> Logger.e(TAG, "Fetch employees failed: ${result.message}")
      else -> {}
    }
  }

  fun dismissCreateReportSheet() {
    _uiState.update { it.copy(showCreateReportSheet = false) }
  }
}
