package com.freyza.employee.core.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SnackbarManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
  single<AppConfig> {
    AppConfig(
      BuildConfig.SUPABASE_URL,
      BuildConfig.SUPABASE_PUBLISHABLE_KEY,
      BuildConfig.API_URL,
    )
  }

  single<HttpClient> {
    HttpClient(Android) {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          prettyPrint = true
          isLenient = false
        })
      }
      install(Logging) {
        level = LogLevel.INFO
      }
    }
  }

  singleOf(::ServerTime)
  singleOf(::SessionManager)
  singleOf(::SnackbarManager)
}
