package com.freyza.employee.util

sealed class Result<out T> {
    data class Success<out T>(val data: T): Result<T>()
    data class Error(val exception: Exception): Result<Nothing>()
    object Progress: Result<Nothing>()
}
