package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.freyza.employee.core.sync.SyncProgressState

@Composable
fun SyncProgressDialog(
  syncState: SyncProgressState,
  onRetry: () -> Unit,
  onDismiss: () -> Unit,
) {
  when (syncState) {
    is SyncProgressState.Syncing -> {
      AlertDialog(
        properties = DialogProperties(
          dismissOnBackPress = false,
          dismissOnClickOutside = false
        ),
        onDismissRequest = { },
        title = { Text(syncState.step.title ?: "Syncing Data") },
        text = {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = syncState.message,
              style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
              progress = { syncState.progressFraction },
              modifier = Modifier.fillMaxWidth(),
            )
            Text(
              text = "Preparing offline data. Please keep the app open.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.secondary,
              textAlign = TextAlign.Center
            )
          }
        },
        confirmButton = {}
      )
    }

    is SyncProgressState.Error -> {
      AlertDialog(
        onDismissRequest = onDismiss,
        title = {
          Text(
            text = "Sync Failed",
            color = MaterialTheme.colorScheme.error
          )
        },
        text = {
          Text(
            text = syncState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
          )
        },
        confirmButton = {
          TextButton(onClick = onRetry) {
            Text("Retry")
          }
        },
        dismissButton = {
          TextButton(onClick = onDismiss) {
            Text("Close")
          }
        }
      )
    }

    else -> {}
  }
}
