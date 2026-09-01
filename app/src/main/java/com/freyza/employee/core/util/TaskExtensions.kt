package com.freyza.employee.core.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
  addOnSuccessListener { result ->
    if (continuation.isActive) {
      continuation.resume(result)
    }
  }

  addOnFailureListener { exception ->
    if (continuation.isActive) {
      continuation.resumeWithException(exception)
    }
  }

  addOnCanceledListener {
    if (continuation.isActive) {
      continuation.cancel(CancellationException("Task was cancelled"))
    }
  }
}
