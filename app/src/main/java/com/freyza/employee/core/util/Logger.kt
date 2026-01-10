package com.freyza.employee.core.util

import android.util.Log

object Logger {
    private const val BASE_TAG = "APP_LOG"

    fun e(tag: String, message: String) {
        Log.e("$BASE_TAG-$tag", message)
    }
    
    fun d(tag: String, message: String) {
        Log.d("$BASE_TAG-$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$BASE_TAG-$tag", message)
    }
}
