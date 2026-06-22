package com.freyza.employee

import android.app.Application
import com.freyza.employee.core.di.appModule
import com.freyza.employee.core.di.repositoryModule
import com.freyza.employee.core.di.supabaseModule
import com.freyza.employee.core.di.useCaseModule
import com.freyza.employee.core.di.viewModelModule
import com.freyza.employee.core.util.HyperlinkedDebugTree
import com.freyza.employee.core.util.ReleaseTree
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class FreyzaEmployeeApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger(Level.ERROR)
      androidContext(this@FreyzaEmployeeApplication)
      modules(
        appModule,
        repositoryModule,
        supabaseModule,
        useCaseModule,
        viewModelModule
      )
    }

    if (BuildConfig.DEBUG) {
      Timber.plant(HyperlinkedDebugTree())
    } else {
      Timber.plant(ReleaseTree())
    }
  }
}
