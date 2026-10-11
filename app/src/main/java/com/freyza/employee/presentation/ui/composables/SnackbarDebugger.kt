package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.core.SnackbarType
import com.freyza.employee.core.util.Logger
import com.freyza.employee.core.util.SnackbarManager
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import org.koin.compose.koinInject

@Composable
fun SnackbarDebugger(
  modifier: Modifier = Modifier,
  snackbarManager: SnackbarManager = koinInject(),
) {
  var showSheet by remember { mutableStateOf(false) }

  if (showSheet) {
    @OptIn(ExperimentalMaterial3Api::class)
    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetGesturesEnabled = true,
    ) {
      Text("Hello Content", modifier = Modifier.padding(16.dp))

      ElevatedButton(onClick = { showSheet = false }) { Text("Hide Sheet") }
    }
  }

  SnackbarDebuggerButtons(
    onShowSnackbar = {
      snackbarManager.showMessage(
        "Hello World!",
        type = SnackbarType.DEFAULT,
        actionLabel = "Yes",
        duration = SnackbarDuration.Long,
        withDismissAction = true,
        onAction = { Logger.d("SnackbarDebugger", "Dismissed snackbar") }
      )
    },
    onShowErrorSnackbar = {
      snackbarManager.showMessage(
        "Hello Error!",
        type = SnackbarType.ERROR,
        actionLabel = "No",
        duration = SnackbarDuration.Long,
        withDismissAction = true,
        onAction = { Logger.d("SnackbarDebugger", "Dismissed error snackbar. Fool!") }
      )
    },
    onShowSuccessSnackbar = {
      snackbarManager.showSuccess("Automatic Success")
    },
    onShowWarningSnackbar = {
      snackbarManager.showMessage(
        "Info",
        type = SnackbarType.WARNING,
        actionLabel = "Ok",
        duration = SnackbarDuration.Long,
        withDismissAction = true,
        onAction = { Logger.d("SnackbarDebugger", "Dismissed warning snackbar") }
      )
    },
    onShowSheet = { showSheet = true },
    modifier = modifier
  )
}

@Composable
private fun SnackbarDebuggerButtons(
  onShowSnackbar: () -> Unit,
  onShowErrorSnackbar: () -> Unit,
  onShowSuccessSnackbar: () -> Unit,
  onShowWarningSnackbar: () -> Unit,
  onShowSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
  ) {
    FilledTonalButton(
      onClick = onShowSnackbar,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
      )
    ) {
      Text("Trigger Snackbar")
    }

    FilledTonalButton(
      onClick = onShowErrorSnackbar,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
      )
    ) {
      Text("Trigger Error Snackbar")
    }

    FilledTonalButton(
      onClick = onShowSuccessSnackbar,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
      )
    ) {
      Text("Trigger Success Snackbar")
    }

    FilledTonalButton(
      onClick = onShowWarningSnackbar,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
      )
    ) {
      Text("Trigger Warning Snackbar")
    }

    ElevatedButton(onClick = onShowSheet) { Text("Show Bottom Sheet") }
  }
}

@Preview(showBackground = true)
@Composable
private fun SnackbarDebuggerPreview() {
  FreyzaEmployeeTheme {
    SnackbarDebuggerButtons(
      onShowSnackbar = {},
      onShowSuccessSnackbar = {},
      onShowErrorSnackbar = {},
      onShowWarningSnackbar = {},
      onShowSheet = {}
    )
  }
}
