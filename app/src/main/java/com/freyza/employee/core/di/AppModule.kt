package com.freyza.employee.core.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.SnackbarManager
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
