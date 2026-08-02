package com.freyza.employee.core.network

import com.freyza.employee.core.util.Logger
import io.ktor.client.plugins.api.createClientPlugin

class ConnectivityPluginConfig {
  lateinit var networkMonitor: NetworkMonitor
}

val ConnectivityPlugin = createClientPlugin("ConnectivityPlugin", ::ConnectivityPluginConfig) {
  onRequest { request, _ ->
    if (!this@createClientPlugin.pluginConfig.networkMonitor.isCurrentlyConnected) {
      Logger.d("ConnectivityPlugin", "Blocked offline request to: ${request.url}")
      throw OfflineException()
    }
  }
}
