package com.freyza.employee.data.di

import com.freyza.employee.data.repository.ExpenseRepository
import com.freyza.employee.data.repository.FakeExpenseRepository
import com.freyza.employee.data.repository.FakeUserRepository
import com.freyza.employee.data.repository.UserRepository
import com.freyza.employee.ui.dashboard.DashboardViewModel
import com.freyza.employee.ui.login.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
//    single { SupabaseClient() }
//    single<UserRepository> { SupabaseUserRepository(get()) }

    factory<ExpenseRepository> { FakeExpenseRepository() }
    factory<UserRepository> { FakeUserRepository() }

    viewModel { DashboardViewModel(get()) }
    viewModel { LoginViewModel(get()) }
}
