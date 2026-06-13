package com.freyza.employee.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ServerStatusDto(
  var commitRef: String,
  val serverTime: String,
  val version: String? = null
)
