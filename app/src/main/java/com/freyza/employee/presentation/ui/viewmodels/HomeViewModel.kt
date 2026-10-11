package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.sync.SyncGroup
import com.freyza.employee.core.sync.SyncManager
import com.freyza.employee.core.sync.SyncProgressState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.TravelPlan
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportParams
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.presentation.ui.state.HomeScreenUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
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
  private val syncManager: SyncManager,
) : ViewModel() {

  companion object {
    const val TAG = "HomeViewModel"
  }

  private var refreshJob: Job? = null
  private var createReportJob: Job? = null

  private val _uiState = MutableStateFlow(HomeScreenUiState(today = serverTime.nowLocalDateTime()))

  // helper data class for combining Room emissions
  private data class RoomData(
    val locations: List<Location> = emptyList(),
    val routes: List<RouteWithLocation> = emptyList(),
    val employees: List<User> = emptyList(),
    val travelPlan: TravelPlan? = null,
    val travelPlanEntry: TravelPlanEntry? = null,
  )

  // shared travelplan flow
  private val travelPlanFlow =
    combine(
      sessionManager.currentEmployee.filterNotNull(),
      serverTime.isSynced
    ) { user, _ -> user }
      .flatMapLatest { user ->
        travelPlanRepository.observeCurrentTravelPlan(user.id, serverTime.todayIn())
      }.distinctUntilChangedBy { it?.id }.onEach { plan ->
        if (plan != null) loadTravelPlanMetrics(plan.id)
      }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

  // derived entry flow
  private val travelPlanEntryFlow = travelPlanFlow.flatMapLatest { plan ->
    if (plan != null) {
      travelPlanRepository.observeTodayTravelPlanEntry(plan.id, serverTime.todayIn())
    } else {
      flowOf(null)
    }
  }

  // combined room streams
  private val roomDataFlow = combine(
    locationRepository.observeAllLocations(),
    routeRepository.observeAllRoutesWithLocation(),
    userRepository.observeAllEmployees(),
    travelPlanFlow,
    travelPlanEntryFlow,
  ) { locations, routes, allEmployees, plan, entry ->
    val currentEmpId = sessionManager.currentEmployee.value?.id
    val employees =
      if (currentEmpId != null) allEmployees.filter { it.id != currentEmpId } else allEmployees
    RoomData(locations, routes, employees, plan, entry)
  }

  // final reactive UI state
  val uiState =
    combine(_uiState, roomDataFlow, syncManager.syncProgress) { state, room, syncProgress ->
      val isSyncing = syncProgress is SyncProgressState.Syncing

      state.copy(
        isRefreshing = state.isRefreshing || isSyncing,
        locations = room.locations,
        routes = room.routes,
        employees = room.employees,
        currentTravelPlan = room.travelPlan,
        todayTravelPlanEntry = room.travelPlanEntry,
        todayPlanEntryRoute = room.routes.find { it.id == room.travelPlanEntry?.routeId },
        todayReportRoute = room.routes.find { it.id == state.currentDailyReport?.routeId }
      )
    }.onStart {
      refresh()
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = HomeScreenUiState(today = serverTime.nowLocalDateTime())
    )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.i(TAG, "Init")
    observeServerTime()
  }

  private fun observeServerTime() {
    viewModelScope.launch {
      serverTime.isSynced.collect { synced ->
        if (synced) {
          _uiState.update { it.copy(today = serverTime.nowLocalDateTime()) }
        }
      }
    }
  }

  fun refresh(forceRefresh: Boolean = false) {
    Logger.i(TAG, "Refreshing data, forceRefresh:$forceRefresh")
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    if (refreshJob?.isActive == true) {
      Logger.d(TAG, "Refresh already in progress, skipping duplicate request")
      return
    }

    syncManager.performSync(targets = if (forceRefresh) SyncGroup.TRANSACTIONAL else SyncGroup.ALL, forceRefresh = forceRefresh)

    refreshJob = viewModelScope.launch {
      _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
      loadCurrentDailyReport(employeeId, forceRefresh)
      _uiState.update { it.copy(isRefreshing = false, isLoading = false) }
    }
  }

  private suspend fun loadTravelPlanMetrics(tpId: String, forceRefresh: Boolean = false) {
    _uiState.update { it.copy(isMetricsLoading = true) }
    when (val result =
      travelPlanRepository.getTravelPlanMetrics(tpId, forceRefresh = forceRefresh)) {
      is Result.Success -> _uiState.update {
        it.copy(
          travelPlanMetrics = result.data,
          isMetricsLoading = false
        )
      }

      is Result.Error -> {
        _uiState.update { it.copy(isMetricsLoading = false) }
        Logger.e(TAG, "Fetch travel plan metrics failed: ${result.message}")
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
          )
        }
        report?.routeId?.let { routeId ->
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
    // Avoid race conditions if Room hasn't hydrated the UI State yet by querying Repo directly
    val routeResult = routeRepository.getRoute(routeId)
    val route = (routeResult as? Result.Success)?.data ?: return
    val locationIds = listOf(route.srcLocId, route.destLocId).distinct()

    when (val result = dailyReportRepository.getPoisByLocation(locationIds, forceRefresh)) {
      is Result.Success -> _uiState.update { it.copy(pois = result.data) }
      is Result.Error -> Logger.e(TAG, "Fetch POIs failed: ${result.message}")
      else -> {}
    }
  }

  fun createCurrentDailyReport(
    dayType: DayType,
    srcLocId: String?,
    destLocId: String?,
    travellingWithId: String? = null,
  ) {
    val employeeId = sessionManager.currentEmployee.value?.id ?: return

    if (createReportJob?.isActive == true) {
      Logger.d(TAG, "Create report already in progress")
      return
    }

    createReportJob = viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      var finalRouteId: String? = null

      if (dayType == DayType.WORK && srcLocId != null && destLocId != null) {
        // Look up route in fully hydrated downstream uiState
        val localRoute = uiState.value.routes.find {
          it.srcLoc.id == srcLocId && it.destLoc.id == destLocId
        }

        if (localRoute != null) {
          finalRouteId = localRoute.id
        } else {
          when (val routeResult = routeRepository.getOrCreateRoute(srcLocId, destLocId)) {
            is Result.Success -> {
              val newRoute = routeResult.data
              finalRouteId = newRoute.id
              Logger.w(TAG, "New route created: ID=${newRoute.id}")
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
          serverTime.todayIn(),
          employeeId,
          dayType,
          finalRouteId,
          travellingWithId
        )
      )

      when (result) {
        is Result.Success -> {
          Logger.i(TAG, "Daily report created successfully id: ${result.data.id}")
          _uiState.update {
            it.copy(
              isLoading = false,
              currentDailyReport = result.data,
              showCreateReportSheet = false,
              todayReportDayType = result.data.dayType
            )
          }
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

  fun dismissCreateReportSheet() {
    _uiState.update { it.copy(showCreateReportSheet = false) }
  }
}
