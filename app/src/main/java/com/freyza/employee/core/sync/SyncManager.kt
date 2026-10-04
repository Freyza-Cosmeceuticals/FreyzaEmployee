package com.freyza.employee.core.sync

import com.freyza.employee.core.Constants
import com.freyza.employee.core.Result
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.data.local.dao.AppMetaDao
import com.freyza.employee.data.local.dao.LocationDao
import com.freyza.employee.data.local.dao.PoiDao
import com.freyza.employee.data.local.dao.RouteDao
import com.freyza.employee.data.local.dao.SyncStateDao
import com.freyza.employee.data.local.dao.TravelPlanDao
import com.freyza.employee.data.local.dao.UserDao
import com.freyza.employee.data.local.entities.AppMetaEntity
import com.freyza.employee.data.local.entities.SyncStateEntity
import com.freyza.employee.data.local.entities.SyncStatus
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncManager(
  private val locationRepository: LocationRepository,
  private val routeRepository: RouteRepository,
  private val userRepository: UserRepository,
  private val travelPlanRepository: TravelPlanRepository,
  private val dailyReportRepository: DailyReportRepository,
  private val sessionManager: SessionManager,
  private val serverTime: ServerTime,
  private val syncStateDao: SyncStateDao,
  private val appMetaDao: AppMetaDao,
  private val locationDao: LocationDao,
  private val routeDao: RouteDao,
  private val userDao: UserDao,
  private val travelPlanDao: TravelPlanDao,
  private val poiDao: PoiDao,
) {

  companion object {
    private const val TAG = "SyncManager"

    const val KEY_LOCATIONS = "LOCATIONS"
    const val KEY_ROUTES = "ROUTES"
    const val KEY_EMPLOYEES = "EMPLOYEES"
    const val KEY_TRAVEL_PLAN = "TRAVEL_PLAN"
  }

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val syncMutex = Mutex()

  private val _syncProgress = MutableStateFlow<SyncProgressState>(SyncProgressState.Idle)
  val syncProgress: StateFlow<SyncProgressState> = _syncProgress.asStateFlow()

  init {
    // collect and log syncProgress
    scope.launch {
      syncProgress.collect { state ->
        when (state) {
          is SyncProgressState.Idle -> Logger.d(TAG, "SyncProgress: Idle")
          is SyncProgressState.Syncing -> Logger.i(
            TAG,
            "SyncProgress: [${state.step.name}] ${state.message} (${(state.progressFraction * 100).toInt()}%)"
          )

          is SyncProgressState.Success -> Logger.i(TAG, "SyncProgress: Success - ${state.message}")
          is SyncProgressState.Error -> Logger.e(TAG, "SyncProgress: Error - ${state.message}")
        }
      }
    }
  }

  fun performSyncIfNeeded(forceRefresh: Boolean = false) {
    scope.launch {
      val user = sessionManager.currentEmployee.filterNotNull().first()

      syncMutex.withLock {
        val now = System.currentTimeMillis()
        val employeeId = user.id

        // check TTLs for time sync
        checkAppMetaTTLs(now, forceRefresh)

        // query cached entities via repositories to check missing status
        val locationsEmpty =
          (locationRepository.getAllLocations(forceRefresh = false) as? Result.Success)?.data?.isEmpty() == true
        val routesEmpty =
          (routeRepository.getAllRoutes(forceRefresh = false) as? Result.Success)?.data?.isEmpty() == true
        val employeesEmpty =
          (userRepository.getAllEmployees(forceRefresh = false) as? Result.Success)?.data?.isEmpty() == true

        val today = serverTime.todayIn()
        val currentPlanResult =
          travelPlanRepository.getCurrentTravelPlan(employeeId, forceRefresh = false)
        val currentPlan = (currentPlanResult as? Result.Success)?.data

        val travelPlanNeverSynced = syncStateDao.getSyncState(KEY_TRAVEL_PLAN) == null
        val travelPlanStale =
          isDomainStale(
            KEY_TRAVEL_PLAN,
            now,
            forceRefresh,
            Constants.TTL_TRAVEL_PLAN_MS
          ) || travelPlanNeverSynced

        val locationsStale = isDomainStale(
          KEY_LOCATIONS,
          now,
          forceRefresh,
          Constants.TTL_LOCATIONS_MS
        ) || locationsEmpty
        val routesStale =
          isDomainStale(KEY_ROUTES, now, forceRefresh, Constants.TTL_ROUTES_MS) || routesEmpty
        val employeesStale = isDomainStale(
          KEY_EMPLOYEES,
          now,
          forceRefresh,
          Constants.TTL_EMPLOYEES_MS
        ) || employeesEmpty

        // collect target locations for POI sync (HQ + Today's plan entry route locations)
        val todayEntryResult = currentPlan?.let {
          travelPlanRepository.getTodayTravelPlanEntry(
            it.id,
            forceRefresh = false
          )
        }
        val todayEntry = (todayEntryResult as? Result.Success)?.data
        val todayRoute = todayEntry?.routeId?.let {
          (routeRepository.getRoute(
            it,
            forceRefresh = false
          ) as? Result.Success)?.data
        }

        val targetPoiLocIds = listOfNotNull(
          user.hqId,
          todayRoute?.srcLocId,
          todayRoute?.destLocId
        ).filter { it.isNotBlank() }.distinct()

        val poisStale = targetPoiLocIds.any { locId ->
          isDomainStale("POIS_LOC_$locId", now, forceRefresh, Constants.TTL_POIS_MS)
        }

        if (!locationsStale && !routesStale && !employeesStale && !travelPlanStale && !poisStale) {
          Logger.d(TAG, "All local data is fresh in Room. Skipping sync.")
          return@launch
        }

        Logger.i(TAG, "Starting sync (forceRefresh=$forceRefresh)...")

        // key to sync action
        val tasks = mutableListOf<Pair<SyncStep, suspend () -> Unit>>()
        if (locationsStale) {
          tasks.add(SyncStep.LOCATIONS to { syncDomain(KEY_LOCATIONS) { locationRepository.refreshLocations() } })
        }
        if (routesStale) {
          tasks.add(SyncStep.ROUTES to { syncDomain(KEY_ROUTES) { routeRepository.refreshRoutes() } })
        }
        if (employeesStale) {
          tasks.add(SyncStep.EMPLOYEES to { syncDomain(KEY_EMPLOYEES) { userRepository.refreshEmployees() } })
        }
        if (travelPlanStale) {
          tasks.add(SyncStep.TRAVEL_PLAN to {
            syncDomain(KEY_TRAVEL_PLAN) {
              travelPlanRepository.refreshTravelPlan(employeeId, today)
            }
          })
        }
        if (poisStale) {
          tasks.add(SyncStep.POIS to { syncPois(targetPoiLocIds, forceRefresh) })
        }

        val totalTasks = tasks.size
        var completedTasks = 0

        _syncProgress.value = SyncProgressState.Syncing(
          step = SyncStep.INITIALIZING,
          message = "Preparing sync...",
          progressFraction = 0.0f
        )

        try {
          for ((step, action) in tasks) {
            val progress = completedTasks.toFloat() / totalTasks.toFloat()
            _syncProgress.value = SyncProgressState.Syncing(
              step = step,
              message = step.title,
              progressFraction = progress
            )
            action()
            completedTasks++
          }

          _syncProgress.value = SyncProgressState.Syncing(
            step = SyncStep.COMPLETED,
            message = "Sync completed",
            progressFraction = 1.0f
          )
          _syncProgress.value = SyncProgressState.Success("Data synced successfully")
        } catch (e: Exception) {
          Logger.e(TAG, "Sync failed: ${e.message}", e)
          _syncProgress.value = SyncProgressState.Error(e.message ?: "Failed to sync data")
        }
      }
    }
  }

  suspend fun nukeAndRebuildDatabase() {
    syncMutex.withLock {
      Logger.w(TAG, "Master Resync requested: Nuking database and rebuilding...")
      locationDao.clearAll()
      routeDao.clearAll()
      userDao.clearAll()
      travelPlanDao.clearAllPlans()
      travelPlanDao.clearAllEntries()
      poiDao.clearAll()
      syncStateDao.clearAll()
    }
    performSyncIfNeeded(forceRefresh = true)
  }

  fun resetProgressState() {
    _syncProgress.value = SyncProgressState.Idle
  }

  private suspend fun syncPois(locationIds: List<String>, forceRefresh: Boolean) {
    val now = System.currentTimeMillis()
    val distinctIds = locationIds.filter { it.isNotBlank() }.distinct()

    for (locId in distinctIds) {
      val key = "POIS_LOC_$locId"
      if (forceRefresh || isDomainStale(key, now, false, Constants.TTL_POIS_MS)) {
        syncDomain(key) {
          dailyReportRepository.refreshPoisForLocations(listOf(locId))
        }
      }
    }
  }

  private suspend fun syncDomain(key: String, action: suspend () -> Result<Unit>) {
    val now = System.currentTimeMillis()
    val result = action()
    if (result is Result.Success) {
      syncStateDao.upsertSyncState(
        SyncStateEntity(
          key = key,
          lastAttemptAt = now,
          lastSuccessAt = now,
          status = SyncStatus.SUCCESS
        )
      )
    } else if (result is Result.Error) {
      syncStateDao.upsertSyncState(
        SyncStateEntity(
          key = key,
          lastAttemptAt = now,
          status = SyncStatus.FAILED,
          errorMessage = result.message
        )
      )
    }
  }

  private suspend fun checkAppMetaTTLs(now: Long, forceRefresh: Boolean) {
    val meta = appMetaDao.getAppMeta() ?: AppMetaEntity()

    if (forceRefresh || (now - meta.lastTimeSyncAt) > Constants.TTL_TIME_SYNC_MS) {
      Logger.d(TAG, "Syncing server time...")
      serverTime.syncTime(forceRefresh = forceRefresh)
    }
  }

  private suspend fun isDomainStale(
    key: String,
    now: Long,
    forceRefresh: Boolean,
    ttl: Long,
  ): Boolean {
    if (forceRefresh) return true
    val state = syncStateDao.getSyncState(key) ?: return true
    return (now - state.lastSuccessAt) > ttl
  }
}
