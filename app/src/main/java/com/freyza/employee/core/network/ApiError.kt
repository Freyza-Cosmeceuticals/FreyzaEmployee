package com.freyza.employee.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ApiIssuePath(
  val key: JsonElement,
)

@Serializable
data class ApiIssue(
  val kind: String,
  val input: JsonElement? = null,
  val received: String? = null,
  val message: String,
  val path: List<ApiIssuePath> = emptyList(),
)

@Serializable
data class ApiErrorResponse(
  val message: String,
  val data: List<ApiIssue> = emptyList(),
)

fun ApiIssue.getRootField(): String? =
  path.firstOrNull()?.key?.jsonPrimitive?.contentOrNull

fun ApiIssue.getArrayIndex(): Int? =
  path.getOrNull(1)?.key?.jsonPrimitive?.intOrNull

fun ApiIssue.getNestedField(): String? =
  path.getOrNull(2)?.key?.jsonPrimitive?.contentOrNull
