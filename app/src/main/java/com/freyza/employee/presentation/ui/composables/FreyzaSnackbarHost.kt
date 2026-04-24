package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freyza.employee.core.FreyzaSnackbarVisuals
import com.freyza.employee.core.SnackbarType
import com.freyza.employee.core.asSnackbarColors

@Composable
fun FreyzaSnackbarHost(
  hostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  snackbar: @Composable ((SnackbarData) -> Unit)? = null,
) {
  SnackbarHost(hostState, snackbar = { data ->
    val visuals = data.visuals as? FreyzaSnackbarVisuals
    val type = visuals?.type ?: SnackbarType.DEFAULT
    val (containerColor, contentColor) = type.asSnackbarColors()

    if (snackbar === null) {
      Snackbar(
        snackbarData = data,
        shape = RoundedCornerShape(16.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        dismissActionContentColor = contentColor,
        modifier = modifier
      )
    } else {
      snackbar(data)
    }
  })
}

// provides global snackbarHost to sheets and other parts where scaffold snackbar isn't visible.
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
  error("No SnackbarHostState provided")
}
