package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun ReportLockedBadge(isLocked: Boolean, modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.Center, modifier = modifier.background(
      if (isLocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
      RoundedCornerShape(integerResource(R.integer.rounding_radius))
    )
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (isLocked) {
        Icon(
          painter = painterResource(R.drawable.lock_24px),
          contentDescription = "Locked",
          modifier = Modifier
            .padding(vertical = 2.dp)
            .padding(start = 2.dp)
            .size(12.dp)
        )
      }
      Text(
        (if (isLocked) "Locked" else "Not Locked").uppercase(),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.default_spacing))
      )
    }
  }
}

@Preview
@Composable
private fun ReportLockedBadgePreview() {
  FreyzaEmployeeTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      ReportLockedBadge(true)
      ReportLockedBadge(false)
    }
  }
}
