package com.freyza.employee.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.freyza.employee.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM employees ORDER BY name ASC")
  fun getAllEmployeesFlow(): Flow<List<UserEntity>>

  @Query("SELECT COUNT(*) FROM employees")
  fun getEmployeeCountFlow(): Flow<Int>

  @Query("SELECT * FROM employees ORDER BY name ASC")
  suspend fun getAllEmployees(): List<UserEntity>

  @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
  suspend fun getEmployeeById(id: String): UserEntity?

  @Upsert
  suspend fun upsertEmployees(employees: List<UserEntity>)

  @Upsert
  suspend fun upsertEmployee(employee: UserEntity)

  @Query("DELETE FROM employees")
  suspend fun clearAll()
}
