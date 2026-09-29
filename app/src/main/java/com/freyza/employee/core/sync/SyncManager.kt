package com.freyza.employee.core.sync

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.Constants
import com.freyza.employee.core.network.safeApiCall
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.DateFormatter
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
import com.freyza.employee.data.mappers.toDomain
import com.freyza.employee.data.mappers.toEntity
import com.freyza.employee.data.network.dto.LocationDto
import com.freyza.employee.data.network.dto.RouteWithLocationDto
import com.freyza.employee.data.network.dto.TravelPlanDto
import com.freyza.employee.data.network.dto.TravelPlanEntryDto
import com.freyza.employee.data.repository.EmployeesResponse
import com.freyza.employee.domain.model.toRoute
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

class SyncManager(
  private val appMetaDao: AppMetaDao,
  private val syncStateDao: SyncStateDao,
  private val locationDao: LocationDao,
  private val routeDao: RouteDao,
  private val userDao: UserDao,
  private val travelPlanDao: TravelPlanDao,
  private val poiDao: PoiDao,
  private val postgrest: Postgrest,
  private val httpClient: HttpClient,
  private val appConfig: AppConfig,
  private val auth: Auth,
  private val sessionManager: SessionManager,
  private val serverTime: ServerTime,
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

  fun performSyncIfNeeded(forceRefresh: Boolean = false) {
    scope.launch {
      syncMutex.withLock {
        val now = System.currentTimeMillis()
        val employeeId = sessionManager.currentEmployee.value?.id ?: return@launch

        // 1. Check TTLs for version check & time sync
        checkAppMetaTTLs(now, forceRefresh)

        // 2. Check if master data is missing in Room or TTL expired
        val locationsStale = isDomainStale(KEY_LOCATIONS, now, forceRefresh) || locationDao.getAllLocations().isEmpty()
        val routesStale = isDomainStale(KEY_ROUTES, now, forceRefresh) || routeDao.getAllRoutes().isEmpty()
        val employeesStale = isDomainStale(KEY_EMPLOYEES, now, forceRefresh) || userDao.getAllEmployees().isEmpty()

        val today = serverTime.todayIn()
        val currentMonthStr = DateFormatter.format(
          LocalDate(year = today.year, month = today.month, day = 1),
          DateFormatter.FormattingType.MACHINE
        )
        val currentPlan = travelPlanDao.getTravelPlan(employeeId, currentMonthStr)
        val travelPlanStale = isDomainStale(KEY_TRAVEL_PLAN, now, forceRefresh) || currentPlan == null

        if (!locationsStale && !routesStale && !employeesStale && !travelPlanStale) {
          Logger.d(TAG, "All local data is fresh in Room. Skipping sync.")
          return@launch
        }

        Logger.i(TAG, "Starting sync (forceRefresh=$forceRefresh)...")

        val tasks = mutableListOf<Pair<SyncStep, suspend () -> Unit>>()
        if (locationsStale) {
          tasks.add(SyncStep.LOCATIONS to { syncLocations() })
        }
        if (routesStale) {
          tasks.add(SyncStep.ROUTES to { syncRoutes() })
        }
        if (employeesStale) {
          tasks.add(SyncStep.EMPLOYEES to { syncEmployees() })
        }
        if (travelPlanStale) {
          tasks.add(SyncStep.TRAVEL_PLAN to { syncTravelPlan(employeeId, today) })
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

  private suspend fun checkAppMetaTTLs(now: Long, forceRefresh: Boolean) {
    val meta = appMetaDao.getAppMeta() ?: AppMetaEntity()

    // Sync Server Time if TTL expired
    if (forceRefresh || (now - meta.lastTimeSyncAt) > Constants.TTL_TIME_SYNC_MS) {
      Logger.d(TAG, "Syncing server time...")
      serverTime.syncTime(forceRefresh = forceRefresh)
      appMetaDao.upsertAppMeta(meta.copy(lastTimeSyncAt = now))
    }
  }

  private suspend fun isDomainStale(key: String, now: Long, forceRefresh: Boolean): Boolean {
    if (forceRefresh) return true
    val state = syncStateDao.getSyncState(key) ?: return true
    return (now - state.lastSuccessAt) > Constants.TTL_MASTER_DATA_MS
  }

  suspend fun syncLocations() {
    safeApiCall(TAG) {
      val locationDtos = postgrest.from("location").select().decodeList<LocationDto>()
      val entities = locationDtos.map { it.toDomain().toEntity() }
      locationDao.upsertLocations(entities)

      val now = System.currentTimeMillis()
      syncStateDao.upsertSyncState(
        SyncStateEntity(
          key = KEY_LOCATIONS,
          lastAttemptAt = now,
          lastSuccessAt = now,
          status = SyncStatus.SUCCESS
        )
      )
    }
  }

  suspend fun syncRoutes() {
    safeApiCall(TAG) {
      val routesDto = postgrest.from("route").select(
        Columns.raw(
          """
          *,
          srcLoc:location!route_srcLocId_fkey(*),
          destLoc:location!route_destLocId_fkey(*)
         """.trimIndent()
        )
      ).decodeList<RouteWithLocationDto>()

      val routeEntities = routesDto.map { it.toDomain().toRoute().toEntity() }
      val locationEntities = routesDto.flatMap { dto ->
        listOf(dto.srcLoc.toDomain().toEntity(), dto.destLoc.toDomain().toEntity())
      }.distinctBy { it.id }

      locationDao.upsertLocations(locationEntities)
      routeDao.upsertRoutes(routeEntities)

      val now = System.currentTimeMillis()
      syncStateDao.upsertSyncState(
        SyncStateEntity(
          key = KEY_ROUTES,
          lastAttemptAt = now,
          lastSuccessAt = now,
          status = SyncStatus.SUCCESS
        )
      )
    }
  }

  suspend fun syncEmployees() {
    safeApiCall(TAG, auth) { token ->
      val response = httpClient.get("${appConfig.apiUrl}/api/employees") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      if (response.status.isSuccess()) {
        val employeesResponse = response.body<EmployeesResponse>()
        if (employeesResponse.success) {
          val userEntities = employeesResponse.data.map { it.toDomain().toEntity() }
          userDao.upsertEmployees(userEntities)

          val now = System.currentTimeMillis()
          syncStateDao.upsertSyncState(
            SyncStateEntity(
              key = KEY_EMPLOYEES,
              lastAttemptAt = now,
              lastSuccessAt = now,
              status = SyncStatus.SUCCESS
            )
          )
        }
      }
    }
  }

  suspend fun syncTravelPlan(employeeId: String, today: LocalDate) {
    val thisMonth = DateFormatter.format(
      LocalDate(year = today.year, month = today.month, day = 1),
      DateFormatter.FormattingType.MACHINE
    )

    safeApiCall(TAG) {
      val travelPlanDto = postgrest.from("travelPlan").select {
        filter {
          TravelPlanDto::employeeId eq employeeId
          TravelPlanDto::month eq thisMonth
        }
      }.decodeSingleOrNull<TravelPlanDto>()

      if (travelPlanDto != null) {
        val planEntity = travelPlanDto.toDomain().toEntity()

        val entriesDto = postgrest.from("travelPlanEntry").select {
          filter {
            TravelPlanEntryDto::tpId eq planEntity.id
          }
        }.decodeList<TravelPlanEntryDto>()

        val entryEntities = entriesDto.map { it.toDomain().toEntity() }

        travelPlanDao.insertPlanWithEntries(planEntity, entryEntities)

        val now = System.currentTimeMillis()
        syncStateDao.upsertSyncState(
          SyncStateEntity(
            key = KEY_TRAVEL_PLAN,
            lastAttemptAt = now,
            lastSuccessAt = now,
            status = SyncStatus.SUCCESS
          )
        )
      }
    }
  }
}
