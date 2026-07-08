package com.freyza.employee.core.di

import com.freyza.employee.data.repository.AuthenticationRepositoryImpl
import com.freyza.employee.data.repository.DailyReportRepositoryImpl
import com.freyza.employee.data.repository.LocationRepositoryImpl
import com.freyza.employee.data.repository.RouteRepositoryImpl
import com.freyza.employee.data.repository.TravelPlanRepositoryImpl
import com.freyza.employee.data.repository.UserRepositoryImpl
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
  singleOf(::AuthenticationRepositoryImpl) bind AuthenticationRepository::class
  singleOf(::TravelPlanRepositoryImpl) bind TravelPlanRepository::class
  singleOf(::DailyReportRepositoryImpl) bind DailyReportRepository::class
  singleOf(::UserRepositoryImpl) bind UserRepository::class
  singleOf(::LocationRepositoryImpl) bind LocationRepository::class
  singleOf(::RouteRepositoryImpl) bind RouteRepository::class
}
