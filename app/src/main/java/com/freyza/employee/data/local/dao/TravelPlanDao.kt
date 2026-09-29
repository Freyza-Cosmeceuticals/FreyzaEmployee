package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.TravelPlanEntity
import com.freyza.employee.data.local.entities.TravelPlanEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TravelPlanDao {
  @Query("SELECT * FROM travel_plans WHERE employeeId = :employeeId AND month = :month LIMIT 1")
  fun getTravelPlanFlow(employeeId: String, month: String): Flow<TravelPlanEntity?>

  @Query("SELECT * FROM travel_plans WHERE employeeId = :employeeId AND month = :month LIMIT 1")
  suspend fun getTravelPlan(employeeId: String, month: String): TravelPlanEntity?

  @Query("SELECT * FROM travel_plans WHERE id = :id LIMIT 1")
  suspend fun getTravelPlanById(id: String): TravelPlanEntity?

  @Query("SELECT * FROM travel_plan_entries WHERE tpId = :tpId ORDER BY date ASC")
  fun getTravelPlanEntriesFlow(tpId: String): Flow<List<TravelPlanEntryEntity>>

  @Query("SELECT * FROM travel_plan_entries WHERE tpId = :tpId ORDER BY date ASC")
  suspend fun getTravelPlanEntries(tpId: String): List<TravelPlanEntryEntity>

  @Query("SELECT * FROM travel_plan_entries WHERE tpId = :tpId AND date = :date LIMIT 1")
  fun getTodayTravelPlanEntryFlow(tpId: String, date: LocalDate): Flow<TravelPlanEntryEntity?>

  @Query("SELECT * FROM travel_plan_entries WHERE tpId = :tpId AND date = :date LIMIT 1")
  suspend fun getTodayTravelPlanEntry(tpId: String, date: LocalDate): TravelPlanEntryEntity?

  @Upsert
  suspend fun upsertTravelPlan(travelPlan: TravelPlanEntity)

  @Upsert
  suspend fun upsertTravelPlanEntries(entries: List<TravelPlanEntryEntity>)

  @Transaction
  suspend fun insertPlanWithEntries(
    travelPlan: TravelPlanEntity,
    entries: List<TravelPlanEntryEntity>
  ) {
    upsertTravelPlan(travelPlan)
    upsertTravelPlanEntries(entries)
  }

  @Query("DELETE FROM travel_plans")
  suspend fun clearAllPlans()

  @Query("DELETE FROM travel_plan_entries")
  suspend fun clearAllEntries()
}
