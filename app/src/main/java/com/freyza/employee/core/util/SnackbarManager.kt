package com.freyza.employee.core.util

import androidx.compose.material3.SnackbarDuration
import com.freyza.employee.core.SnackbarType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

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
  private val _messages = Channel<SnackbarMessage>(Channel.BUFFERED)
  val messages = _messages.receiveAsFlow()

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
    _messages.trySend(
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
