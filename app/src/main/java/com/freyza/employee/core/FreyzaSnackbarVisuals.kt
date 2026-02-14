package com.freyza.employee.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class SnackbarType {
  SUCCESS,
  ERROR,
  WARNING,
  DEFAULT
}

@Composable
fun SnackbarType.asSnackbarColors(): Pair<Color, Color> =
  when (this) {
    SnackbarType.SUCCESS -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
    SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    SnackbarType.WARNING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
    SnackbarType.DEFAULT -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
  }


data class FreyzaSnackbarVisuals(
  override val message: String,
  override val actionLabel: String? = null,
  override val duration: SnackbarDuration = SnackbarDuration.Short,
  override val withDismissAction: Boolean,
  val type: SnackbarType = SnackbarType.DEFAULT,
) : SnackbarVisuals

suspend fun SnackbarHostState.showTypedSnackbar(
  message: String,
  type: SnackbarType = SnackbarType.DEFAULT,
  actionLabel: String? = null,
  duration: SnackbarDuration = SnackbarDuration.Short,
  withDismissAction: Boolean = false,
  dismissCurrent: Boolean = false,
) {
  if (dismissCurrent) currentSnackbarData?.dismiss()

  showSnackbar(
    FreyzaSnackbarVisuals(
      message = message,
      actionLabel = actionLabel,
      duration = duration,
      withDismissAction = withDismissAction,
      type = type
    )
  )
}
