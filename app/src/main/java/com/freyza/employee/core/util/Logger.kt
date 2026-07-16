package com.freyza.employee.core.util

import timber.log.Timber

object Logger {
  private const val BASE_TAG = "APP"

  fun e(tag: String, message: String, throwable: Throwable? = null) {
    Timber.tag("$BASE_TAG/$tag").e(throwable, message)
  }

  fun d(tag: String, message: String) {
    Timber.tag("$BASE_TAG/$tag").d(message)
  }

  fun w(tag: String, message: String) {
    Timber.tag("$BASE_TAG/$tag").w(message)
  }

  fun i(tag: String, message: String) {
    Timber.tag("$BASE_TAG/$tag").i(message)
  }
}

/**
 * Development Tree: Injects clickable file links into Logcat
 */
class HyperlinkedDebugTree : Timber.DebugTree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val stackTrace = Throwable().stackTrace

    // walk up the stack trace to find the first class that isn't our Logger or Timber itself
    val caller = stackTrace.firstOrNull { element ->
      val className = element.className

      !className.contains("timber.log.Timber") &&
              !className.contains("Logger") &&
              !className.contains("HyperlinkedDebugTree") &&
              className != "dalvik.system.VMStack" &&
              className != "java.lang.Thread" &&
              className != "Method"
    }

    val clickableLink = caller?.let { "(${it.fileName}:${it.lineNumber})" } ?: ""

    val hyperlinkedMessage = if (clickableLink.isNotEmpty()) {
      "$message · $clickableLink"
    } else {
      message
    }

    super.log(priority, tag, hyperlinkedMessage, t)
  }
}
