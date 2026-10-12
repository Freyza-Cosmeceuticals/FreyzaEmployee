package com.freyza.employee.core.util

import androidx.compose.material3.SnackbarDuration
import com.freyza.employee.core.SnackbarType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Data class representing a snackbar message.
 */
data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.DEFAULT,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val withDismissAction: Boolean = false,
    val onAction: (() -> Unit)? = null,
)

/**
 * A centralized manager to handle snackbar messages across the app.
 */
class SnackbarManager {
  private val _messages = MutableSharedFlow<SnackbarMessage>(
    replay = 0,
    extraBufferCapacity = 2,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val messages: SharedFlow<SnackbarMessage> = _messages.asSharedFlow()

  /**
   * Shows a snackbar message.
   */
  fun showMessage(
      message: String,
      type: SnackbarType = SnackbarType.DEFAULT,
      actionLabel: String? = null,
      duration: SnackbarDuration = SnackbarDuration.Short,
      withDismissAction: Boolean = false,
      onAction: (() -> Unit)? = null,
  ) {
    _messages.tryEmit(
      SnackbarMessage(
        message = message,
        type = type,
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = withDismissAction,
        onAction = onAction
      )
    )
  }

  /**
   * Convenience method to show an error message.
   */
  fun showError(message: String) {
    showMessage(message = message, type = SnackbarType.ERROR, withDismissAction = true)
  }

  /**
   * Convenience method to show a success message.
   */
  fun showSuccess(message: String) {
    showMessage(message = message, type = SnackbarType.SUCCESS, withDismissAction = true)
  }
}
