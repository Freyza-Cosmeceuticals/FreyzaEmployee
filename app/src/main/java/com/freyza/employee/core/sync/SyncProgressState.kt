package com.freyza.employee.core.sync

enum class SyncStep(val title: String) {
  INITIALIZING("Initializing Sync"),
  LOCATIONS("Syncing Locations"),
  ROUTES("Syncing Routes"),
  EMPLOYEES("Syncing Employees"),
  TRAVEL_PLAN("Syncing Travel Plan"),
  COMPLETED("Sync Completed")
}

sealed interface SyncProgressState {
  data object Idle : SyncProgressState

  data class Syncing(
    val step: SyncStep,
    val message: String,
    val progressFraction: Float,
  ) : SyncProgressState

  data class Success(val message: String) : SyncProgressState

  data class Error(val message: String) : SyncProgressState
}
