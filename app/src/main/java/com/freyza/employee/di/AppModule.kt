package com.freyza.employee.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.common.MainViewModel
import com.freyza.employee.data.repository.AuthenticationRepository
import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.data.repository.impl.AuthenticationRepositoryImpl
import com.freyza.employee.data.repository.impl.ExpenseRepositoryImpl
import com.freyza.employee.data.repository.impl.UserRepositoryImpl
import com.freyza.employee.domain.usecase.auth.GetUserUseCase
import com.freyza.employee.domain.usecase.auth.LoginUseCase
import com.freyza.employee.domain.usecase.auth.LoginWithGoogleUseCase
import com.freyza.employee.domain.usecase.auth.LogoutUseCase
import com.freyza.employee.domain.usecase.auth.RegisterUseCase
import com.freyza.employee.domain.usecase.auth.impl.GetUserUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.LoginUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.LoginWithGoogleUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.LogoutUseCaseImpl
import com.freyza.employee.domain.usecase.auth.impl.RegisterUseCaseImpl
import com.freyza.employee.domain.usecase.expense.GetAllExpensesUseCase
import com.freyza.employee.domain.usecase.expense.GetRecentExpensesUseCase
import com.freyza.employee.domain.usecase.expense.impl.GetAllExpensesUseCaseImpl
import com.freyza.employee.domain.usecase.expense.impl.GetRecentExpensesUseCaseImpl
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
    single<UserRepository> { UserRepositoryImpl(get()) }
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

    single<GetUserUseCase> { GetUserUseCaseImpl(get()) }

    single<GetAllExpensesUseCase> { GetAllExpensesUseCaseImpl(get()) }
    single<GetRecentExpensesUseCase> { GetRecentExpensesUseCaseImpl(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
}
