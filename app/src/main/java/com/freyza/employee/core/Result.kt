package com.freyza.employee.core

sealed class Result<out T> {
  data class Success<out T>(val data: T, val message: String? = null) : Result<T>()
  data class Error<out T>(
    val message: String,
    val data: T? = null,
    val errorBody: Any? = null,
  ) : Result<T>()

  data class Loading<out T>(val data: T? = null, val message: String? = null) : Result<T>()
}
