package com.freyza.employee.core.di

import com.freyza.employee.core.AppConfig
import io.github.jan.supabase.SupabaseClient
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
    }
  }

  single<Auth> {
    get<SupabaseClient>().auth
  }

  single<Postgrest> {
    get<SupabaseClient>().postgrest
  }
}
