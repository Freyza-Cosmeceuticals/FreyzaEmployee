package com.freyza.employee

import android.app.Application
import com.freyza.employee.core.di.appModule
import com.freyza.employee.core.di.repositoryModule
import com.freyza.employee.core.di.supabaseModule
import com.freyza.employee.core.di.useCaseModule
import com.freyza.employee.core.di.viewModelModule
import com.freyza.employee.core.util.HyperlinkedDebugTree
import io.sentry.SentryLevel
import io.sentry.SentryLogLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class FreyzaEmployeeApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    initLogging()

    startKoin {
      androidLogger(Level.ERROR)
      androidContext(this@FreyzaEmployeeApplication)
      modules(
        appModule, repositoryModule, supabaseModule, useCaseModule, viewModelModule
      )
    }
  }

  private fun initLogging() {
    if (BuildConfig.DEBUG) {
      Timber.plant(HyperlinkedDebugTree())
    } else {
      SentryAndroid.init(this) { options ->
        options.environment = BuildConfig.FLAVOR

        options.dsn = BuildConfig.SENTRY_DSN
        options.tracesSampleRate = 1.0

        options.isEnableUserInteractionTracing = true
        options.isAttachScreenshot = true
        options.isSendDefaultPii = true

        options.logs.isEnabled = true

        options.sessionReplay.sessionSampleRate = 0.1
        options.sessionReplay.onErrorSampleRate = 1.0

        options.addIntegration(
          SentryTimberIntegration(
            minEventLevel = SentryLevel.ERROR,
            minBreadcrumbLevel = SentryLevel.INFO,
            minLogsLevel = SentryLogLevel.INFO
          )
        )
      }
    }
  }
}
