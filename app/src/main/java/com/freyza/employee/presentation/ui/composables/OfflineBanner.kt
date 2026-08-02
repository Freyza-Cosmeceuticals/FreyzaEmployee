package com.freyza.employee.presentation.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.freyza.employee.R

@Composable
fun OfflineBanner(
  isOnline: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = !isOnline,
    enter = expandVertically(expandFrom = Alignment.Top),
    exit = shrinkVertically(shrinkTowards = Alignment.Top)
  ) {
    Surface(
      color = MaterialTheme.colorScheme.errorContainer,
      contentColor = MaterialTheme.colorScheme.onErrorContainer,
      modifier = modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.statusBars)
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.error_24px), contentDescription = "Offline"
          )
          Text(
            text = "You are currently offline", style = MaterialTheme.typography.labelLarge
          )
        }

        TextButton(
          onClick = onRefresh, colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          )
        ) {
          Text("Retry")
        }
      }
    }
  }
}
