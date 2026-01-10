package com.freyza.employee.core

sealed class Result<T>(val data: T? = null, val message: String? = null) {
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Success<T>(data: T? = null) : Result<T>(data)
    class Loading<T>(data: T? = null) : Result<T>(data)
}
