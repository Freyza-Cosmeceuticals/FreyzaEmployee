package com.freyza.employee.core.util

import android.util.Log

object Logger {
  private const val BASE_TAG = "APP"

  private fun formatMessage(message: String): String {
    val e = Throwable().stackTrace[2]
    return "$message · [${e.fileName}:${e.lineNumber} ${e.className.substringAfterLast('.')}#${e.methodName}]"
  }

  fun e(tag: String, message: String) {
    Log.e("$BASE_TAG/$tag", formatMessage(message))
  }

  fun d(tag: String, message: String) {
    Log.d("$BASE_TAG/$tag", formatMessage(message))
  }

  fun i(tag: String, message: String) {
    Log.i("$BASE_TAG/$tag", formatMessage(message))
  }
}
