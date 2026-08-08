package com.freyza.employee.presentation.ui.authenticated.addvisit.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
internal fun TagInputField(
  items: List<String>,
  onItemAdded: (String) -> Unit,
  onItemRemoved: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  error: String? = null,
) {
  var currentText by rememberSaveable { mutableStateOf("") }

  Column(modifier = modifier.fillMaxWidth()) {
    // added chips
    if (items.isNotEmpty()) {
      CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        FlowRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.default_spacing)),
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(
              2
            )
          ),
          verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(
              3
            )
          ),
          itemVerticalAlignment = Alignment.CenterVertically
        ) {
          items.forEach { item ->
            InputChip(
              selected = false,
              onClick = { onItemRemoved(item) },
              label = { Text(item) },
              enabled = enabled,
              trailingIcon = {
                Icon(
                  painter = painterResource(R.drawable.close_small_24px),
                  contentDescription = "Remove",
                  modifier = Modifier.size(16.dp)
                )
              })
          }
        }
      }
    } else {
      Text(
        "No items added",
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = dimensionResource(R.dimen.default_spacing))
      )
    }

    OutlinedTextField(
      value = currentText,
      onValueChange = { input ->
        // Detect comma or newline
        if (input.contains(",") || input.contains("\n")) {
          val newItems = input.split(",", "\n")
          // Add all completed items
          newItems.dropLast(1).forEach {
            if (it.isNotBlank()) onItemAdded(
              it.trim()
              .replaceFirstChar { char -> char.uppercase() })
          }
          // Keep whatever is left after the last comma
          currentText = newItems.last().trimStart()
        } else {
          currentText = input
        }
      },
      label = { Text(label) },
      placeholder = { Text("Type and press comma (,)") },
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      isError = error != null,
      supportingText = error?.let { { Text(it) } },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        if (currentText.isNotBlank()) {
          onItemAdded(currentText.trim().replaceFirstChar { it.uppercase() })
          currentText = ""
        }
      })
    )
  }
}

// Helper to make the whole row clickable for toggles
@Composable
internal fun ToggleableRow(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  text: String,
  enabled: Boolean = true,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onCheckedChange(!checked) }
      .padding(vertical = 4.dp)) {
    Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text,
      style = MaterialTheme.typography.bodyLarge,
      color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.38f
      )
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TagInputFieldPreview() {
  FreyzaEmployeeTheme {
    TagInputField(
      items = listOf("hello", "Hi", "Bye", "Loooooooong", "text here"),
      onItemAdded = {},
      onItemRemoved = {},
      label = "Greetings"
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TagInputFieldEmptyPreview() {
  FreyzaEmployeeTheme {
    TagInputField(
      items = emptyList(), onItemAdded = {}, onItemRemoved = {}, label = "Greetings"
    )
  }
}
