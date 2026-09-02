package com.freyza.employee.core.di

import com.freyza.employee.BuildConfig
import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.GPSMonitor
import com.freyza.employee.core.LocationTracker
import com.freyza.employee.core.network.ConnectivityManagerNetworkMonitor
import com.freyza.employee.core.network.ConnectivityPlugin
import com.freyza.employee.core.network.NetworkMonitor
import com.freyza.employee.core.state.SessionManager
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.SharedPreferencesHelper
import com.freyza.employee.core.util.SnackbarManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
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
    val networkMonitor = get<NetworkMonitor>()
    HttpClient(Android) {
      install(HttpTimeout) {
        requestTimeoutMillis = 15_000L
        connectTimeoutMillis = 10_000L
        socketTimeoutMillis = 10_000L
      }
      install(ConnectivityPlugin) {
        this.networkMonitor = networkMonitor
      }
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

  single<NetworkMonitor> {
    ConnectivityManagerNetworkMonitor(get()).also {
      NetworkMonitor.register(it)
    }
  }

  singleOf(::ServerTime)
  singleOf(::SharedPreferencesHelper)
  singleOf(::SessionManager)
  singleOf(::SnackbarManager)
  singleOf(::GPSMonitor)
  singleOf(::LocationTracker)
}
