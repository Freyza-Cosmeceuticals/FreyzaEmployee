package com.freyza.employee.presentation.ui.composables

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.freyza.employee.R

@Composable
fun FreyzaFabButton(modifier: Modifier = Modifier) {
  FloatingActionButton(
    onClick = {},
    modifier = modifier
  ) {
    Icon(painter = painterResource(R.drawable.empty_dashboard_24px), contentDescription = null)
  }
}
