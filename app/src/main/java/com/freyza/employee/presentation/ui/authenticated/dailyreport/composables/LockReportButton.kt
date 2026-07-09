package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun LockReportButton(
  lockingState: UIState<Unit>,
  onLockPressed: () -> Unit,
  modifier: Modifier = Modifier,
  hasVisits: Boolean = false,
) {
  var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
  var showConfirmNoVisitsDialog by rememberSaveable { mutableStateOf(false) }

  if (showConfirmDialog) {
    ConfirmLockReportDialog(
      message = "Are you sure to lock the report?\nIt cannot be updated after it is locked.",
      onCancel = { showConfirmDialog = false },
      onConfirm = {
        showConfirmDialog = false
        if (hasVisits) onLockPressed()
        else showConfirmNoVisitsDialog = true
      })
  }

  if (showConfirmNoVisitsDialog) {
    ConfirmLockReportDialog(
      message = "The report has no visits.\nAre you still sure?",
      onCancel = { showConfirmNoVisitsDialog = false },
      onConfirm = {
        showConfirmNoVisitsDialog = false
        showConfirmDialog = false

        onLockPressed()
      })
  }

  Button(
    onClick = { showConfirmDialog = true },
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.errorContainer,
      contentColor = contentColorFor(MaterialTheme.colorScheme.errorContainer),
      disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
      disabledContentColor = contentColorFor(MaterialTheme.colorScheme.errorContainer),
    ),
    enabled = lockingState !is UIState.Loading,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (lockingState is UIState.Loading) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        strokeWidth = 3.dp,
        color = contentColorFor(MaterialTheme.colorScheme.errorContainer).copy(alpha = 0.6f)
      )
    } else {
      Row(
        horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing), Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(dimensionResource(R.dimen.default_spacing))
      ) {
        Icon(
          painter = painterResource(R.drawable.lock_24px),
          contentDescription = "lock report",
          modifier = Modifier
            .size(18.dp)
            .alignBy(FirstBaseline)
        )
        Text("Lock Report", modifier = Modifier)
      }
    }
  }
}

@Composable
fun ConfirmLockReportDialog(
  message: String,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    onDismissRequest = onCancel,
    title = { Text("Lock Report") },
    text = {
      Text(message)
    },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
      ) { Text("Lock") }
    },
    dismissButton = {
      TextButton(onClick = onCancel) { Text("Cancel") }
    },
    modifier = modifier,
  )
}

@Preview(showBackground = true)
@Composable
private fun LockReportButtonPreview() {
  FreyzaEmployeeTheme {
    LockReportButton(lockingState = UIState.Idle(), onLockPressed = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun LockReportButtonPreviewLoading() {
  FreyzaEmployeeTheme {
    LockReportButton(lockingState = UIState.Loading(), onLockPressed = {})
  }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ConfirmLockReportDialogPreview() {
  FreyzaEmployeeTheme {
    ConfirmLockReportDialog(
      message = "Locking the report please allow me to",
      onConfirm = {},
      onCancel = {})
  }
}
