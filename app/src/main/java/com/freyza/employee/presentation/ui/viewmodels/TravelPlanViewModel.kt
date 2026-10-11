package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.UIState
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.sync.SyncManager
import com.freyza.employee.core.sync.SyncStep
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.presentation.ui.state.TravelPlanUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TravelPlanViewModel(
  private val sessionManager: SessionManager,
  private val routeRepository: RouteRepository,
  private val travelPlanRepository: TravelPlanRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
  private val syncManager: SyncManager,
) : ViewModel() {

  companion object {
    const val TAG = "TravelPlanViewModel"
  }

  private val _transientState =
    MutableStateFlow(TravelPlanUiState(today = serverTime.nowLocalDateTime()))

  private val travelPlanFlow =
    combine(
      sessionManager.currentEmployee.filterNotNull(),
      serverTime.isSynced
    ) { user, _ -> user }
      .flatMapLatest { user ->
        val today = serverTime.todayIn()
        travelPlanRepository.observeCurrentTravelPlan(user.id, today)
      }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

  val uiState: StateFlow<TravelPlanUiState> = combine(
    _transientState,
    routeRepository.observeAllRoutesWithLocation(),
    travelPlanFlow,
  ) { state, routes, plan ->
    val entries = plan?.travelPlanEntries ?: emptyList()
    state.copy(
      routes = routes,
      currentTravelPlan = UIState.Ready(plan),
      travelPlanEntries = UIState.Ready(entries),
    )
  }.onStart {
    refresh()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = TravelPlanUiState(today = serverTime.nowLocalDateTime())
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
          _transientState.update { it.copy(today = nowTime) }
        }
      }
    }
  }

  fun refresh(forceRefresh: Boolean = false) {
    syncManager.performSync(targets = setOf(SyncStep.TRAVEL_PLAN), forceRefresh = forceRefresh)
  }

  fun loadSelectedPlanEntryRoute(tpEntryId: String) {
    Logger.d(TAG, "Fetching plan entry route data for tpEntryId:$tpEntryId")

    _transientState.update {
      it.copy(
        selectedRoute = UIState.Loading(it.selectedRoute.data)
      )
    }

    viewModelScope.launch {
      val availableRoutes = uiState.value.routes ?: emptyList()
      val availableEntries = uiState.value.travelPlanEntries.data ?: emptyList()

      val thisTravelPlan = availableEntries.find { it.id == tpEntryId }
      val thisRoute = availableRoutes.find { it.id == thisTravelPlan?.routeId }

      if (thisRoute != null) {
        _transientState.update {
          it.copy(selectedRoute = UIState.Ready(thisRoute))
        }
        Logger.d(TAG, "${thisRoute.id} route set successfully")
      } else {
        _transientState.update {
          it.copy(selectedRoute = UIState.Error("Route not found for plan entry"))
        }
      }
    }
  }
}
