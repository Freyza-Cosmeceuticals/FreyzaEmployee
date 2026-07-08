package com.freyza.employee.core.di

import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.auth.RegisterUseCase
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule = module {
  factoryOf(::LoginUseCase)
  factoryOf(::RegisterUseCase)
  factoryOf(::LoginWithGoogleUseCase)
  factoryOf(::LogoutUseCase)

  factoryOf(::GetTodayDailyReportUseCase)
  factoryOf(::CreateTodayDailyReportUseCase)
  factoryOf(::GetRecentDailyReportsUseCase)
  factoryOf(::CreateVisitUseCase)
  factoryOf(::LockReportUseCase)
}
