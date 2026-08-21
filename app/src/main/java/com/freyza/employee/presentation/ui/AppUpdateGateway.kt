package com.freyza.employee.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.domain.model.AppUpdateInfo
import com.freyza.employee.domain.model.dummyAppUpdateInfoAvailable
import com.freyza.employee.presentation.ui.state.AppUpdateState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.AppUpdateViewModel

@Composable
fun AppUpdateGateway(
  viewModel: AppUpdateViewModel,
  content: @Composable () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  when (val state = uiState) {
    is AppUpdateState.UpdateAvailable -> {
      AppUpdateScreen(
        updateInfo = state.updateInfo,
        state = state,
        onPrimaryAction = { viewModel.startDownload() },
        onDismiss = { viewModel.dismissUpdate() }
      )
    }

    is AppUpdateState.Downloading -> {
      AppUpdateScreen(
        updateInfo = state.updateInfo,
        state = state,
        onPrimaryAction = { viewModel.startDownload() },
        onDismiss = { viewModel.dismissUpdate() }
      )
    }

    is AppUpdateState.ReadyToInstall -> {
      AppUpdateScreen(
        updateInfo = state.updateInfo,
        state = state,
        onPrimaryAction = { viewModel.installUpdate() },
        onDismiss = { viewModel.dismissUpdate() }
      )
    }

    is AppUpdateState.Error -> {
      AppUpdateScreen(
        updateInfo = null,
        state = state,
        onPrimaryAction = { viewModel.dismissUpdate() },
        onDismiss = { viewModel.dismissUpdate() }
      )
    }

    is AppUpdateState.Idle, AppUpdateState.Checking -> {
      content()
    }
  }
}

@Composable
private fun AppUpdateScreen(
  updateInfo: AppUpdateInfo?,
  state: AppUpdateState,
  onPrimaryAction: () -> Unit,
  onDismiss: (() -> Unit)?,
) {
  val isMandatory = updateInfo?.isMandatory == true

  BackHandler(enabled = true) {
    if (!isMandatory && state !is AppUpdateState.Downloading && onDismiss != null) {
      onDismiss()
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Header Icon
        Icon(
          painter = when (state) {
            is AppUpdateState.Error -> painterResource(R.drawable.error_24px)
            is AppUpdateState.UpdateAvailable -> painterResource(R.drawable.system_update_alt_24px)
            is AppUpdateState.Downloading -> painterResource(R.drawable.mobile_arrow_down_24px)
            is AppUpdateState.ReadyToInstall -> painterResource(R.drawable.mobile_check_24px)
            else -> painterResource(R.drawable.system_update_alt_24px)
          },
          contentDescription = null,
          tint = if (state is AppUpdateState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
          text = when {
            state is AppUpdateState.Error -> "Update Failed"
            state is AppUpdateState.ReadyToInstall -> "Update Ready to Install"
            state is AppUpdateState.Downloading -> "Downloading Update..."
            isMandatory -> "Mandatory Update Required"
            else -> "App Update Available"
          },
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Version details / description
        if (updateInfo != null) {
          Text(
            text = "Version ${updateInfo.versionName}-${updateInfo.buildNumber}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )

          updateInfo.fileSizeMb?.let { size ->
            Text(
              text = "Size: %.1f MB".format(size),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Release notes / Error container
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            if (state is AppUpdateState.Error) {
              Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
              )
            } else if (updateInfo != null && updateInfo.releaseNotes.isNotBlank()) {
              Text(
                text = "What's New:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = updateInfo.releaseNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              Text(
                text = "A new update with performance improvements and bug fixes is ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Progress Bar (During download)
        if (state is AppUpdateState.Downloading) {
          Spacer(modifier = Modifier.height(24.dp))
          LinearProgressIndicator(
            progress = { state.progress / 100f },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "${state.progress.toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = onPrimaryAction,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            enabled = state !is AppUpdateState.Downloading
          ) {
            Text(
              text = when (state) {
                is AppUpdateState.Error -> "Retry"
                is AppUpdateState.ReadyToInstall -> "Install Now"
                is AppUpdateState.Downloading -> "Downloading..."
                else -> "Download & Install"
              }
            )
          }

          if (!isMandatory && state !is AppUpdateState.Downloading && onDismiss != null) {
            OutlinedButton(
              onClick = onDismiss,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Text(if (state !is AppUpdateState.Error) "Close" else "Later")
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun AppUpdateScreenPreview() {
  FreyzaEmployeeTheme {
    AppUpdateScreen(
      updateInfo = dummyAppUpdateInfoAvailable(),
      state = AppUpdateState.UpdateAvailable(dummyAppUpdateInfoAvailable()),
      onPrimaryAction = {},
      onDismiss = {}
    )
  }
}

@Preview
@Composable
private fun AppUpdateScreenMandatoryPreview() {
  FreyzaEmployeeTheme {
    AppUpdateScreen(
      updateInfo = dummyAppUpdateInfoAvailable(mandatory = true),
      state = AppUpdateState.UpdateAvailable(dummyAppUpdateInfoAvailable(mandatory = true)),
      onPrimaryAction = {},
      onDismiss = {}
    )
  }
}

@Preview
@Composable
private fun AppUpdateScreenDownloadingPreview() {
  FreyzaEmployeeTheme {
    AppUpdateScreen(
      updateInfo = dummyAppUpdateInfoAvailable(),
      state = AppUpdateState.Downloading(
        updateInfo = dummyAppUpdateInfoAvailable(),
        progress = 34.6f
      ),
      onPrimaryAction = {},
      onDismiss = {}
    )
  }
}

@Preview
@Composable
private fun AppUpdateScreenReadyPreview() {
  FreyzaEmployeeTheme {
    AppUpdateScreen(
      updateInfo = dummyAppUpdateInfoAvailable(),
      state = AppUpdateState.ReadyToInstall(dummyAppUpdateInfoAvailable()),
      onPrimaryAction = {},
      onDismiss = {}
    )
  }
}

@Preview
@Composable
private fun AppUpdateScreenErrorPreview() {
  FreyzaEmployeeTheme {
    AppUpdateScreen(
      updateInfo = dummyAppUpdateInfoAvailable(),
      state = AppUpdateState.Error("Some error occurred"),
      onPrimaryAction = {},
      onDismiss = {}
    )
  }
}
