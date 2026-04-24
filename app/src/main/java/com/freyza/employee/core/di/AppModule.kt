package com.freyza.employee.core.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.data.repository.AuthenticationRepositoryImpl
import com.freyza.employee.data.repository.DailyReportRepositoryImpl
import com.freyza.employee.data.repository.LocationRepositoryImpl
import com.freyza.employee.data.repository.RouteRepositoryImpl
import com.freyza.employee.data.repository.TravelPlanRepositoryImpl
import com.freyza.employee.data.repository.UserRepositoryImpl
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.repository.DailyReportRepository
import com.freyza.employee.domain.repository.LocationRepository
import com.freyza.employee.domain.repository.RouteRepository
import com.freyza.employee.domain.repository.TravelPlanRepository
import com.freyza.employee.domain.repository.UserRepository
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.auth.RegisterUseCase
import com.freyza.employee.domain.usecase.auth.impl.LoginUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.LoginWithGoogleUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.LogoutUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.RegisterUseCaseImpl
import com.freyza.employee.domain.usecase.dailyreport.CreateTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.CreateVisitUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetRecentDailyReportsUseCase
import com.freyza.employee.domain.usecase.dailyreport.GetTodayDailyReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.LockReportUseCase
import com.freyza.employee.domain.usecase.dailyreport.impl.CreateTodayDailyReportUseCaseImpl
import com.freyza.employee.domain.usecase.dailyreport.impl.CreateVisitUseCaseImpl
import com.freyza.employee.domain.usecase.dailyreport.impl.GetRecentDailyReportsUseCaseImpl
import com.freyza.employee.domain.usecase.dailyreport.impl.GetTodayDailyReportUseCaseImpl
import com.freyza.employee.domain.usecase.dailyreport.impl.LockReportUseCaseImpl
import com.freyza.employee.domain.usecase.location.GetLocationUseCase
import com.freyza.employee.domain.usecase.location.impl.GetLocationUseCaseImpl
import com.freyza.employee.domain.usecase.route.GetAllRoutesWithLocationUseCase
import com.freyza.employee.domain.usecase.route.GetRouteUseCase
import com.freyza.employee.domain.usecase.route.impl.GetAllRoutesWithLocationUseCaseImpl
import com.freyza.employee.domain.usecase.route.impl.GetRouteUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTodayTravelPlanEntryUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanEntriesUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.impl.GetCurrentTravelPlanUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.impl.GetTodayTravelPlanEntryUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.impl.GetTravelPlanEntriesUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.impl.GetTravelPlanUseCaseImpl
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import com.freyza.employee.domain.usecase.user.impl.GetCurrentUserUseCaseImpl
import com.freyza.employee.domain.usecase.user.impl.GetUserUseCaseImpl
import com.freyza.employee.presentation.ui.viewmodels.AddVisitViewModel
import com.freyza.employee.presentation.ui.viewmodels.DailyReportViewModel
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.presentation.ui.viewmodels.MainViewModel
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import com.freyza.employee.presentation.ui.viewmodels.TravelPlanViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.postgrest
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
  single<AppConfig> {
    AppConfig(
      BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY
    )
  }

  single<SessionManager> {
    SessionManager()
  }

  single<SnackbarManager> {
    SnackbarManager()
  }
}

val repositoryModule = module {
  single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
  single<TravelPlanRepository> { TravelPlanRepositoryImpl(get()) }
  single<DailyReportRepository> { DailyReportRepositoryImpl(get()) }
  single<UserRepository> { UserRepositoryImpl(get(), get()) }
  single<LocationRepository> { LocationRepositoryImpl(get()) }
  single<RouteRepository> { RouteRepositoryImpl(get()) }
}

val supabaseModule = module {
  single<SupabaseClient> {
    createSupabaseClient(
      supabaseUrl = get<AppConfig>().supabaseUrl,
      supabaseKey = get<AppConfig>().supabasePublishableKey
    ) {
      install(Auth) {
        flowType = FlowType.PKCE
        scheme = "app"
        host = "supabase.com"
      }
      install(Postgrest) {
        propertyConversionMethod = PropertyConversionMethod.NONE
      }
    }
  }

  single<Auth> {
    get<SupabaseClient>().auth
  }

  single<Postgrest> {
    get<SupabaseClient>().postgrest
  }

}

val useCaseModule = module {
  single<LoginUseCase> { LoginUseCaseImpl(get()) }
  single<RegisterUseCase> { RegisterUseCaseImpl(get()) }
  single<LoginWithGoogleUseCase> { LoginWithGoogleUseCaseImpl(get()) }
  single<LogoutUseCase> { LogoutUseCaseImpl(get()) }

  single<GetUserUseCase> { GetUserUseCaseImpl(get()) }
  single<GetCurrentUserUseCase> { GetCurrentUserUseCaseImpl(get()) }

  single<GetTravelPlanUseCase> { GetTravelPlanUseCaseImpl(get()) }
  single<GetCurrentTravelPlanUseCase> { GetCurrentTravelPlanUseCaseImpl(get()) }
  single<GetTodayTravelPlanEntryUseCase> { GetTodayTravelPlanEntryUseCaseImpl(get()) }
  single<GetTravelPlanEntriesUseCase> { GetTravelPlanEntriesUseCaseImpl(get()) }

  single<GetTodayDailyReportUseCase> { GetTodayDailyReportUseCaseImpl(get()) }
  single<CreateTodayDailyReportUseCase> { CreateTodayDailyReportUseCaseImpl(get()) }
  single<GetRecentDailyReportsUseCase> { GetRecentDailyReportsUseCaseImpl(get()) }
  single<CreateVisitUseCase> { CreateVisitUseCaseImpl(get()) }
  single<LockReportUseCase> { LockReportUseCaseImpl(get()) }

  single<GetLocationUseCase> { GetLocationUseCaseImpl(get()) }
  single<GetRouteUseCase> { GetRouteUseCaseImpl(get()) }
  single<GetAllRoutesWithLocationUseCase> { GetAllRoutesWithLocationUseCaseImpl(get()) }
}

val viewModelModule = module {
  viewModel { MainViewModel(get(), get(), get(), get()) }
  viewModel { LoginViewModel(get(), get(), get()) }
  viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  viewModel { TravelPlanViewModel(get(), get(), get(), get(), get()) }
  viewModel { DailyReportViewModel(get(), get(), get(), get(), get()) }
  viewModel { (visitType: VisitType, reportId: String, employeeId: String) ->
    AddVisitViewModel(
      visitType,
      reportId,
      employeeId,
      get(),
      get(),
      get()
    )
  }
  viewModel { ProfileViewModel(get(), get()) }
}
