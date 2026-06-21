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
import org.koin.dsl.module

val repositoryModule = module {
  single<AuthenticationRepository> { AuthenticationRepositoryImpl(get(), get(), get()) }
  single<TravelPlanRepository> { TravelPlanRepositoryImpl(get(), get()) }
  single<DailyReportRepository> { DailyReportRepositoryImpl(get()) }
  single<UserRepository> { UserRepositoryImpl(get(), get()) }
  single<LocationRepository> { LocationRepositoryImpl(get()) }
  single<RouteRepository> { RouteRepositoryImpl(get()) }
}
