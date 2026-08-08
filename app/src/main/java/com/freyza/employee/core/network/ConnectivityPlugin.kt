package com.freyza.employee.core.network

import com.freyza.employee.core.util.Logger
import io.ktor.client.plugins.api.createClientPlugin
import io.sentry.Breadcrumb
import io.sentry.Sentry

class ConnectivityPluginConfig {
  lateinit var networkMonitor: NetworkMonitor
}

val ConnectivityPlugin = createClientPlugin("ConnectivityPlugin", ::ConnectivityPluginConfig) {
  onRequest { request, _ ->
    if (!this@createClientPlugin.pluginConfig.networkMonitor.isCurrentlyConnected) {
      Logger.d("ConnectivityPlugin", "Blocked offline request to: ${request.url}")
      Sentry.addBreadcrumb(Breadcrumb().apply {
        category = "network"
        message = "Blocked offline request to ${request.url}"
      })
      throw OfflineException()
    }
  }
}
