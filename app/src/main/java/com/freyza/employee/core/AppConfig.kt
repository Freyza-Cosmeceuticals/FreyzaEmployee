package com.freyza.employee.core

/**
 * Application Configuration sourced from build time variables.
 */
data class AppConfig(
  val supabaseUrl: String,
  val supabasePublishableKey: String,
  val apiUrl: String
)
