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
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanEntriesUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanUseCase
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import org.koin.dsl.module

val useCaseModule = module {
  factory { LoginUseCase(get()) }
  factory { RegisterUseCase(get()) }
  factory { LoginWithGoogleUseCase(get()) }
  factory { LogoutUseCase(get()) }

  factory { GetUserUseCase(get()) }
  factory { GetCurrentUserUseCase(get()) }

  factory { GetTravelPlanUseCase(get()) }
  factory { GetCurrentTravelPlanUseCase(get()) }
  factory { GetTodayTravelPlanEntryUseCase(get()) }
  factory { GetTravelPlanEntriesUseCase(get()) }

  factory { GetTodayDailyReportUseCase(get()) }
  factory { CreateTodayDailyReportUseCase(get()) }
  factory { GetRecentDailyReportsUseCase(get()) }
  factory { CreateVisitUseCase(get()) }
  factory { LockReportUseCase(get()) }

  factory { GetLocationUseCase(get()) }
  factory { GetRouteUseCase(get()) }
  factory { GetAllRoutesWithLocationUseCase(get()) }
}
