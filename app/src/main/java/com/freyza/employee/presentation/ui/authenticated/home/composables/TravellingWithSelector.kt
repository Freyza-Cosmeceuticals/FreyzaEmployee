package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.domain.model.dummyUserEmployeeAlt
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravellingWithSelector(
  employees: List<User>,
  selectedEmployee: User?,
  onEmployeeSelect: (User?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDialog by remember { mutableStateOf(false) }

  OutlinedCard(
    onClick = { showDialog = true },
    shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (selectedEmployee != null) MaterialTheme.colorScheme.primaryContainer.copy(
        alpha = 0.3f
      )
      else MaterialTheme.colorScheme.surface
    ),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .padding(dimensionResource(R.dimen.screen_padding))
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        painter = painterResource(R.drawable.account_circle_24px),
        contentDescription = null,
        tint = if (selectedEmployee != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Travelling With",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = selectedEmployee?.name ?: "No One (Solo)",
          style = MaterialTheme.typography.bodyLarge,
          color = if (selectedEmployee != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
      }

      if (selectedEmployee != null) {
        IconButton(
          onClick = { onEmployeeSelect(null) }, modifier = Modifier.size(24.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.close_24px),
            contentDescription = "Clear selection",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        Icon(
          painter = painterResource(R.drawable.arrow_drop_down_24px),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }

  if (showDialog) {
    TravellingWithSelectorAlertDialog(
      employees = employees,
      selectedEmployee = selectedEmployee,
      onEmployeeSelect = onEmployeeSelect,
      onDismiss = { showDialog = false })
  }
}

@Composable
private fun TravellingWithSelectorAlertDialog(
  employees: List<User>,
  selectedEmployee: User?,
  onEmployeeSelect: (User?) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select Travelling With") },
    text = {
      LazyColumn(
        modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        item("option_no_one") {
          ListItem(
            headlineContent = { Text("No One (Solo)") }, leadingContent = {
              RadioButton(
                selected = selectedEmployee == null, onClick = null
              )
            }, modifier = Modifier
              .clip(
                RoundedCornerShape(
                  size = if (selectedEmployee == null) integerResource(R.integer.rounding_radius).dp else 8.dp
                )
              )
              .clickable {
                onEmployeeSelect(null)
                onDismiss()
              })
        }

        items(employees, key = { it.id }) { employee ->
          ListItem(
            headlineContent = { Text("${employee.name} (${employee.tier?.name})") },
            leadingContent = {
              RadioButton(
                selected = selectedEmployee?.id == employee.id, onClick = null
              )
            },
            modifier = Modifier
              .clip(
                RoundedCornerShape(
                  size = if (selectedEmployee?.id == employee.id) integerResource(
                    R.integer.rounding_radius
                  ).dp else 8.dp
                )
              )
              .clickable {
                onEmployeeSelect(employee)
                onDismiss()
              })
        }
      }
    },
    confirmButton = {
      TextButton(onClick = { onDismiss() }) {
        Text("Close")
      }
    })
}

@Preview(showBackground = true)
@Composable
private fun TravellingWithSelectorSelectedPreview() {
  FreyzaEmployeeTheme {
    TravellingWithSelector(
      employees = listOf(dummyUserEmployee()),
      selectedEmployee = dummyUserEmployee(),
      onEmployeeSelect = { })
  }
}


@Preview(showBackground = true)
@Composable
private fun TravellingWithSelectorNoOnePreview() {
  FreyzaEmployeeTheme {
    TravellingWithSelector(
      employees = listOf(
        dummyUserEmployee()
      ), selectedEmployee = null, onEmployeeSelect = { })
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TravellingWithSelectorAlertDialogPreview() {
  FreyzaEmployeeTheme {
    TravellingWithSelectorAlertDialog(
      employees = listOf(
        dummyUserEmployee(), dummyUserEmployeeAlt()
      ), selectedEmployee = dummyUserEmployee(), onEmployeeSelect = {}, onDismiss = {})
  }
}
