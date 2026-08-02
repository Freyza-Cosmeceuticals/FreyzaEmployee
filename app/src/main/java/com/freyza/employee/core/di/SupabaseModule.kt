package com.freyza.employee.core.di

import com.freyza.employee.core.AppConfig
import com.freyza.employee.core.network.ConnectivityPlugin
import com.freyza.employee.core.network.NetworkMonitor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.postgrest
import org.koin.dsl.module

val supabaseModule = module {
  single<SupabaseClient> {
    val networkMonitor = get<NetworkMonitor>()

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

      @OptIn(SupabaseInternal::class) httpConfig {
        install(ConnectivityPlugin) {
          this.networkMonitor = networkMonitor
        }
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
