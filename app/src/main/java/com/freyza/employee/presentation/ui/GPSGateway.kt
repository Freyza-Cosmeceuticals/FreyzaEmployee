package com.freyza.employee.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.freyza.employee.R
import com.freyza.employee.core.GPSMonitor
import com.freyza.employee.core.util.Logger
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import org.koin.compose.koinInject

@Composable
fun GPSGateway(
  modifier: Modifier = Modifier,
  gpsMonitor: GPSMonitor = koinInject(),
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val activity = context as? Activity

  // Manage GPS Monitor
  DisposableEffect(gpsMonitor) {
    gpsMonitor.startMonitoring()
    onDispose { gpsMonitor.stopMonitoring() }
  }

  val isGpsEnabled by gpsMonitor.isGpsEnabled.collectAsState()
  val requiredPermissions = arrayOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
  )

  fun checkPermissions(): Boolean {
    return requiredPermissions.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
  }

  var hasPermissions by remember { mutableStateOf(checkPermissions()) }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    hasPermissions = checkPermissions()
  }

  // Permission Launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = { results ->
      // Update state to true only if all requested permissions were granted
      Logger.d("GPSGateway", "Request permission result: $results")
      hasPermissions = results.values.all { it }
    })

  BackHandler(enabled = !hasPermissions || !isGpsEnabled) {
    Logger.d("GPSGateway", "Back pressed during block. Exiting app.")
    activity?.finishAffinity()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // content is always in the background
    content()

    when {
      !hasPermissions -> {
        Logger.d("GPSGateway", "Don't have permissions, requesting")
        Scaffold {
          PermissionBlockerScreen(
            onRequestPermission = { permissionLauncher.launch(requiredPermissions) },
            modifier = modifier.padding(it)
          )
        }
      }

      !isGpsEnabled -> {
        Logger.d("GPSGateway", "GPS is disabled, blocking")
        Scaffold {
          GpsBlockerScreen(
            context = context, modifier = modifier.padding(it)
          )
        }
      }
    }
  }
}

@Composable
private fun PermissionBlockerScreen(
  onRequestPermission: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(dimensionResource(R.dimen.screen_padding).times(2)),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      painter = painterResource(R.drawable.location_on_24px),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
      text = "Location Access Required", style = MaterialTheme.typography.titleLarge
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "This app requires precise location permissions to log visits. Please grant access to continue.",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
      Text("Grant Permission")
    }
  }
}

@Composable
private fun GpsBlockerScreen(
  context: Context,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(dimensionResource(R.dimen.screen_padding).times(2)),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      painter = painterResource(R.drawable.location_disabled_24px),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
      text = "GPS is Disabled", style = MaterialTheme.typography.titleLarge
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Please turn on your device's location services (GPS) to use the app.",
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
      onClick = {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
      }, modifier = Modifier.fillMaxWidth()
    ) {
      Text("Open Settings")
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PermissionBlockerScreenPreview() {
  FreyzaEmployeeTheme {
    PermissionBlockerScreen(onRequestPermission = {})
  }
}


@Preview(showBackground = true)
@Composable
private fun GpsBlockerScreenPreview() {
  FreyzaEmployeeTheme {
    GpsBlockerScreen(context = LocalContext.current)
  }
}
