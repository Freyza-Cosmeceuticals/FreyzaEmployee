package com.freyza.employee.core.di

import androidx.room.Room
import com.freyza.employee.data.local.db.FreyzaDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
  single<FreyzaDatabase> {
    Room.databaseBuilder(
      androidContext(),
      FreyzaDatabase::class.java,
      "freyza_employee_db"
    )
      .fallbackToDestructiveMigration(true)
      .build()
  }

  single { get<FreyzaDatabase>().appMetaDao() }
  single { get<FreyzaDatabase>().syncStateDao() }
  single { get<FreyzaDatabase>().locationDao() }
  single { get<FreyzaDatabase>().routeDao() }
  single { get<FreyzaDatabase>().userDao() }
  single { get<FreyzaDatabase>().travelPlanDao() }
  single { get<FreyzaDatabase>().poiDao() }
}
