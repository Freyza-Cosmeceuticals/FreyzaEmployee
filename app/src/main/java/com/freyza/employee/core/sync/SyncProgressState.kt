package com.freyza.employee.core.sync

enum class SyncStep(val title: String) {
  INITIALIZING("Initializing Sync"),
  LOCATIONS("Syncing Locations"),
  ROUTES("Syncing Routes"),
  EMPLOYEES("Syncing Employees"),
  TRAVEL_PLAN("Syncing Travel Plan"),
  POIS("Syncing Points of Interest"),
  COMPLETED("Sync Completed")
}

object SyncGroup {
  val ALL: Set<SyncStep> = setOf(
    SyncStep.LOCATIONS,
    SyncStep.ROUTES,
    SyncStep.EMPLOYEES,
    SyncStep.TRAVEL_PLAN,
    SyncStep.POIS
  )

  val MASTER_DATA: Set<SyncStep> = setOf(
    SyncStep.LOCATIONS,
    SyncStep.ROUTES,
    SyncStep.EMPLOYEES
  )

  val TRANSACTIONAL: Set<SyncStep> = setOf(
    SyncStep.TRAVEL_PLAN,
    SyncStep.POIS
  )
}

sealed interface SyncProgressState {
  data object Idle : SyncProgressState

  data class Syncing(
    val step: SyncStep,
    val message: String,
    val progressFraction: Float,
    val showModalDialog: Boolean = false,
  ) : SyncProgressState

  data class Success(val message: String) : SyncProgressState

  data class Error(
    val message: String,
    val showModalDialog: Boolean = false,
  ) : SyncProgressState
}
