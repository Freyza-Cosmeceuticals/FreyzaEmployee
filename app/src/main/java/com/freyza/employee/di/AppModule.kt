package com.freyza.employee.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.common.MainViewModel
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.data.repository.impl.AuthenticationRepositoryImpl
import com.freyza.employee.data.repository.impl.ExpenseRepositoryImpl
import com.freyza.employee.domain.usecase.LoginUseCase
import com.freyza.employee.domain.usecase.LoginWithGoogleUseCase
import com.freyza.employee.domain.usecase.LogoutUseCase
import com.freyza.employee.domain.usecase.RegisterUseCase
import com.freyza.employee.domain.usecase.impl.LoginUseCaseImpl
import com.freyza.employee.domain.usecase.impl.LoginWithGoogleUseCaseImpl
import com.freyza.employee.domain.usecase.impl.LogoutUseCaseImpl
import com.freyza.employee.domain.usecase.impl.RegisterUseCaseImpl
import com.freyza.employee.presentation.ui.viewmodels.HomeViewModel
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
}

val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "app"
                host = "supabase.com"
            }
            install(Postgrest)
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
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
}
