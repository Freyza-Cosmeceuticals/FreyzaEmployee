package com.freyza.employee.presentation.ui.state

import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.data.local.entities.AppMetaEntity
import com.freyza.employee.data.local.entities.SyncStateEntity
import com.freyza.employee.data.local.entities.SyncStatus
import kotlinx.datetime.LocalDateTime

data class ProfileScreenUiState(
  val today: LocalDateTime,
  val hqName: String? = null,
  val locationCount: Int = 0,
  val routeCount: Int = 0,
  val employeeCount: Int = 0,
  val travelPlanCount: Int = 0,
  val travelPlanEntryCount: Int = 0,
  val poiCount: Int = 0,
  val syncStates: List<SyncStateEntity> = emptyList(),
  val appMeta: AppMetaEntity? = null,
)

fun dummyProfileScreenUiState(serverTime: ServerTime = ServerTime()): ProfileScreenUiState =
  ProfileScreenUiState(
    today = serverTime.nowLocalDateTime(),
    hqName = "HQ",
    locationCount = 5,
    routeCount = 10,
    employeeCount = 20,
    travelPlanCount = 9,
    travelPlanEntryCount = 192,
    poiCount = 838,
    syncStates = listOf(
      SyncStateEntity("key1", 1, 1, SyncStatus.SUCCESS),
      SyncStateEntity("key2", 5, 1, SyncStatus.FAILED, "Failed to sync"),
      SyncStateEntity("key3", 8, 1, SyncStatus.SYNCING),
      SyncStateEntity("key3", 1, 1, SyncStatus.IDLE)
    ),
    appMeta = AppMetaEntity(1, "0.0.0", 1, 2, 3)
  )
