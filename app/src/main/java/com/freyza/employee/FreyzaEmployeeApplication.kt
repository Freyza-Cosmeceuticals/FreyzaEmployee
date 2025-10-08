package com.freyza.employee

import android.app.Application
import com.freyza.employee.di.repositoryModule
import com.freyza.employee.di.supabaseModule
import com.freyza.employee.di.useCaseModule
import com.freyza.employee.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FreyzaEmployeeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FreyzaEmployeeApplication)
            modules(repositoryModule, supabaseModule, useCaseModule, viewModelModule)
        }
    }
}
