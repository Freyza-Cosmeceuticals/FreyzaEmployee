package com.freyza.employee.presentation.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.sync.SyncGroup
import com.freyza.employee.core.sync.SyncManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.data.local.dao.AppMetaDao
import com.freyza.employee.data.local.dao.LocationDao
import com.freyza.employee.data.local.dao.PoiDao
import com.freyza.employee.data.local.dao.RouteDao
import com.freyza.employee.data.local.dao.SyncStateDao
import com.freyza.employee.data.local.dao.TravelPlanDao
import com.freyza.employee.data.local.dao.UserDao
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
  private val sessionManager: SessionManager,
  private val locationRepository: LocationRepository,
  private val snackbarManager: SnackbarManager,
  private val serverTime: ServerTime,
  private val locationDao: LocationDao,
  private val routeDao: RouteDao,
  private val userDao: UserDao,
  private val poiDao: PoiDao,
  private val travelPlanDao: TravelPlanDao,
  private val syncStateDao: SyncStateDao,
  private val appMetaDao: AppMetaDao,
  private val syncManager: SyncManager,
) : ViewModel() {

  companion object {
    const val TAG = "ProfileViewModel"
  }

  private val _transientState = MutableStateFlow(ProfileScreenUiState(today = serverTime.nowLocalDateTime()))

  private val countsFlow = combine(
    locationDao.getLocationCountFlow(),
    routeDao.getRouteCountFlow(),
    userDao.getEmployeeCountFlow(),
    poiDao.getPoiCountFlow(),
    travelPlanDao.getTravelPlanCountFlow(),
  ) { loc, route, emp, poi, plan ->
    listOf(loc, route, emp, poi, plan)
  }

  val uiState: StateFlow<ProfileScreenUiState> = combine(
    _transientState,
    countsFlow,
    syncStateDao.getAllSyncStatesFlow(),
    appMetaDao.getAppMetaFlow(),
  ) { state, counts, syncStates, meta ->
    state.copy(
      locationCount = counts[0],
      routeCount = counts[1],
      employeeCount = counts[2],
      poiCount = counts[3],
      travelPlanCount = counts[4],
      syncStates = syncStates,
      appMeta = meta,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = ProfileScreenUiState(today = serverTime.nowLocalDateTime())
  )

  val currentUser = sessionManager.currentEmployee

  init {
    Logger.d(TAG, "Init")
    observeServerTime()
    loadHqName()
  }

  private fun observeServerTime() {
    viewModelScope.launch {
      serverTime.isSynced.collect { synced ->
        if (synced) {
          _transientState.update { it.copy(today = serverTime.nowLocalDateTime()) }
        }
      }
    }
  }

  private fun loadHqName() {
    val hqId = sessionManager.currentEmployee.value?.hqId ?: return
    viewModelScope.launch {
      when (val result = locationRepository.getLocation(hqId)) {
        is Result.Success -> {
          _transientState.update { it.copy(hqName = result.data?.name) }
        }

        is Result.Error -> {
          Logger.e(TAG, "Error loading HQ name", Error(result.message))
        }

        else -> {}
      }
    }
  }

  fun forceResyncMasterData() {
    Logger.i(TAG, "User triggered force resync of master data")
    syncManager.performSync(targets = SyncGroup.ALL, forceRefresh = true)
    snackbarManager.showSuccess("Syncing all data...")
  }

  fun triggerForceSync() {
    forceResyncMasterData()
  }

  fun nukeAndRebuildDatabase() {
    Logger.w(TAG, "User triggered nuke and rebuild of database")
    viewModelScope.launch {
      syncManager.nukeAndRebuildDatabase()
      snackbarManager.showSuccess("Database reset and rebuild started...")
    }
  }

  fun triggerNukeAndRebuild() {
    nukeAndRebuildDatabase()
  }
}
