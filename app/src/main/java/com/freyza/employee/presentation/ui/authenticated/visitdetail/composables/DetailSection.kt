package com.freyza.employee.presentation.ui.authenticated.visitdetail.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
  }
}

@Composable
fun DetailSection(title: String, @DrawableRes icon: Int, content: @Composable () -> Unit) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary
        )
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
      content()
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DetailRowPreview() {
  FreyzaEmployeeTheme {
    DetailRow(label = "Visit Date", value = "Oct 24, 2023")
  }
}

@Preview(showBackground = true)
@Composable
private fun DetailSectionPreview() {
  FreyzaEmployeeTheme {
    DetailSection(
      title = "Basic Information",
      icon = R.drawable.description_24px
    ) {
      Column {
        DetailRow(label = "Visit Date", value = "Oct 24, 2023")
        DetailRow(label = "Visit Type", value = "Doctor")
      }
    }
  }
}
