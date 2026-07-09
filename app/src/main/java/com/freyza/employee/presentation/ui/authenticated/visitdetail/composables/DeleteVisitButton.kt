package com.freyza.employee.presentation.ui.authenticated.visitdetail.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun DeleteVisitButton(onDeleteVisit: () -> Unit, modifier: Modifier = Modifier) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    ConfirmDeleteVisitDialog(
      message = "Are you sure you want to delete this visit? This action cannot be undone.",
      onCancel = { showDeleteDialog = false },
      onConfirm = {
        showDeleteDialog = false
        onDeleteVisit()
      })
  }

  OutlinedButton(
    onClick = { showDeleteDialog = true },
    modifier = modifier.fillMaxWidth(),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
  ) {
    Icon(painterResource(R.drawable.close_24px), contentDescription = null)
    Text("Delete Visit", modifier = Modifier.padding(start = 8.dp))
  }
}


@Composable
fun ConfirmDeleteVisitDialog(
  message: String,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    onDismissRequest = onCancel,
    title = { Text("Delete Visit") },
    text = {
      Text(message)
    },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
      ) { Text("Delete") }
    },
    dismissButton = {
      TextButton(onClick = onCancel) { Text("Cancel") }
    },
    modifier = modifier,
  )
}

@Preview(showBackground = true)
@Composable
private fun DeleteVisitButtonPreview() {
  FreyzaEmployeeTheme {
    DeleteVisitButton(onDeleteVisit = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConfirmDeleteVisitDialogPreview() {
  FreyzaEmployeeTheme {
    ConfirmDeleteVisitDialog(
      message = "Are you sure you want to delete this visit? This action cannot be undone.",
      onCancel = {},
      onConfirm = {})
  }
}
