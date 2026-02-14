package com.freyza.employee.core

sealed class UIState<T>(val data: T? = null, val message: String? = null) {
    class Idle<T>(data: T? = null) : UIState<T>(data)
    class Loading<T>(data: T? = null, message: String? = null) : UIState<T>(data, message)
    class Ready<T>(data: T) : UIState<T>(data)
    class Error<T>(message: String, data: T? = null) : UIState<T>(data, message)
}
