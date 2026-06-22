package com.freyza.employee.core.util

import android.util.Log
import timber.log.Timber

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

  fun w(tag: String, message: String) {
    Log.w("$BASE_TAG/$tag", formatMessage(message))
  }

  fun i(tag: String, message: String) {
    Log.i("$BASE_TAG/$tag", formatMessage(message))
  }
}

/**
 * Development Tree: Injects clickable file links into Logcat
 */
class HyperlinkedDebugTree : Timber.DebugTree() {
  override fun createStackElementTag(element: StackTraceElement): String {
    // Formats the tag so Android Studio makes it a clickable link
    // Example output: [HomeViewModel:(HomeViewModel.kt:145)]
    return "${super.createStackElementTag(element)}:(${element.fileName}:${element.lineNumber})"
  }
}

/**
 * Production Tree: Silences basic logs, passes Errors to Sentry
 */
class ReleaseTree : Timber.Tree() {

  // Intercept before any string formatting occurs
  override fun isLoggable(tag: String?, priority: Int): Boolean {
    return priority == Log.WARN || priority == Log.ERROR
  }

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (!isLoggable(tag, priority)) return

    // Optional: If you add Firebase Crashlytics or Sentry later, route errors here!
    // if (priority == Log.ERROR) {
    //     t?.let { Crashlytics.recordException(it) }
    //     Crashlytics.log(message)
    // }

    // For now, just print the serious errors to the system log
    Log.println(priority, tag ?: "FreyzaRelease", message)
  }
}
