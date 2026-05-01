package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.freyza.employee.BuildConfig
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun VersionInfo(modifier: Modifier = Modifier) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.secondary
    )

    @Suppress("KotlinConstantConditions")
    // these are build flavors, generated when switching
    when (BuildConfig.FLAVOR) {
      "dev" -> {
        Text(
          "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      "preview" -> {
        Text(
          "${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }

      "release" -> {}
    }

    if (BuildConfig.DEBUG) {
      Text(
        "Debug Build".uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Red.copy(alpha = 0.4f)
      )
    }
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun VersionInfoPreview() {
  FreyzaEmployeeTheme {
    VersionInfo(Modifier.fillMaxWidth())
  }
}
