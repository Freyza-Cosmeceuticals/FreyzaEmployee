package com.freyza.employee.presentation.ui.authenticated.addvisit.composables

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.core.UIState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun SubmitVisitButton(
  onSubmitVisit: () -> Unit,
  creationState: UIState<Boolean>,
  modifier: Modifier = Modifier,
  text: String = "Save Visit",
) {
  var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
  val canSubmit = creationState.data == true && creationState !is UIState.Loading

  if (showConfirmDialog) {
    ConfirmSubmitReportDialog(
      title = "Save Visit",
      message = "Are you sure want to save this visit?",
      enabled = canSubmit,
      onCancel = { showConfirmDialog = false },
      onConfirm = {
        showConfirmDialog = false
        if (canSubmit) onSubmitVisit()
      })
  }

  return Button(
    onClick = { showConfirmDialog = true },
    modifier = modifier,
    enabled = canSubmit && ((creationState is UIState.Idle) || (creationState is UIState.Error)),
  ) {
    if (creationState is UIState.Loading) {
      CircularProgressIndicator(modifier = Modifier.size(24.dp))
    } else {
      Text(text)
    }
  }
}

@Composable
fun ConfirmSubmitReportDialog(
  title: String,
  message: String,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  AlertDialog(
    onDismissRequest = onCancel,
    title = { Text(title) },
    text = {
      Text(message)
    },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
      ) { Text("Save") }
    },
    dismissButton = {
      TextButton(
        onClick = onCancel,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
      ) { Text("Let me edit") }
    },
    modifier = modifier,
  )
}

@Preview(showBackground = true)
@Composable
private fun SubmitVisitButtonPreview() {
  FreyzaEmployeeTheme {
    SubmitVisitButton(onSubmitVisit = {}, creationState = UIState.Idle())
  }
}

@Preview(showBackground = true)
@Composable
private fun SubmitVisitButtonLoadingPreview() {
  FreyzaEmployeeTheme {
    SubmitVisitButton(onSubmitVisit = {}, creationState = UIState.Loading())
  }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ConfirmSubmitReportDialogPreview() {
  FreyzaEmployeeTheme {
    ConfirmSubmitReportDialog(
      title = "Submit Title",
      message = "Submiting the visit please allow me to",
      onConfirm = {},
      onCancel = {})
  }
}
