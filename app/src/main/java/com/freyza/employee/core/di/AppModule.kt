package com.freyza.employee.core.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.data.repository.AuthenticationRepositoryImpl
import com.freyza.employee.data.repository.ExpenseRepositoryImpl
import com.freyza.employee.data.repository.TravelPlanRepositoryImpl
import com.freyza.employee.data.repository.UserRepositoryImpl
import com.freyza.employee.domain.repository.AuthenticationRepository
import com.freyza.employee.domain.repository.ExpenseRepository
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
import com.freyza.employee.domain.usecase.expense.GetAllExpensesUseCase
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.expense.impl.GetAllExpensesUseCaseImpl
import com.freyza.employee.domain.usecase.expense.impl.GetRecentExpensesUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.GetCurrentTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.GetTravelPlanUseCase
import com.freyza.employee.domain.usecase.travelplan.impl.GetCurrentTravelPlanUseCaseImpl
import com.freyza.employee.domain.usecase.travelplan.impl.GetTravelPlanUseCaseImpl
import com.freyza.employee.domain.usecase.user.GetCurrentUserUseCase
import com.freyza.employee.domain.usecase.user.GetUserUseCase
import com.freyza.employee.domain.usecase.user.impl.GetCurrentUserUseCaseImpl
import com.freyza.employee.domain.usecase.user.impl.GetUserUseCaseImpl
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.presentation.ui.viewmodels.MainViewModel
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

val appConfigModule = module {
    single<AppConfig> {
        AppConfig(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_PUBLISHABLE_KEY
        )
    }
}

val repositoryModule = module {
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<TravelPlanRepository> { TravelPlanRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
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

    single<GetAllExpensesUseCase> { GetAllExpensesUseCaseImpl(get()) }
    single<GetRecentExpensesUseCase> { GetRecentExpensesUseCaseImpl(get()) }

    single<GetTravelPlanUseCase> { GetTravelPlanUseCaseImpl(get()) }
    single<GetCurrentTravelPlanUseCase> { GetCurrentTravelPlanUseCaseImpl(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
}
