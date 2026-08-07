package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
  label: String,
  items: List<T>,
  selectedItem: T?,
  onItemSelect: (T?) -> Unit,
  itemLabeler: (T) -> String,
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "Select option",
  leadingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  supportingText: @Composable (() -> Unit)? = null,
  enabled: Boolean = true,
) {
  var expanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  LaunchedEffect(selectedItem) {
    if (selectedItem != null) {
      val labelText = itemLabeler(selectedItem)
      if (query != labelText) {
        onQueryChange(labelText)
      }
    }
  }

  val filteredItems = remember(query, items) {
    if (query.isEmpty()) {
      items
    } else {
      items.filter { itemLabeler(it).contains(query, ignoreCase = true) }
    }
  }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { if (enabled) expanded = !expanded },
    modifier = modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = query,
      onValueChange = {
        onQueryChange(it)
        expanded = true
        val matchedItem = items.find { item -> itemLabeler(item).equals(it, ignoreCase = true) }
        if (matchedItem != selectedItem) {
          onItemSelect(matchedItem)
        }
      },
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      enabled = enabled,
      isError = isError,
      supportingText = supportingText,
      leadingIcon = leadingIcon,
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
      },
      suffix = {
        Icon(
          painter = painterResource(R.drawable.close_small_24px),
          contentDescription = "Clear",
          modifier = Modifier.clickable {
            onItemSelect(null)
            onQueryChange("")
            expanded = true
          })
      },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      modifier = Modifier
        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
        .fillMaxWidth(),
//      shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
      singleLine = true
    )

    if (filteredItems.isNotEmpty()) {
      ExposedDropdownMenu(
        expanded = expanded, scrollState = scrollState, onDismissRequest = { expanded = false }) {
        filteredItems.forEach { item ->
          DropdownMenuItem(
            text = { Text(itemLabeler(item)) }, onClick = {
              onItemSelect(item)
              onQueryChange(itemLabeler(item))
              expanded = false
            },
            trailingIcon = {
              if (selectedItem == item)
                Icon(
                  painter = painterResource(R.drawable.check_small_24px),
                  contentDescription = null
                )
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SearchableDropdownPreview() {
  FreyzaEmployeeTheme {
    SearchableDropdown(
      label = "Search Stuff",
      items = listOf("Item 1", "Item 2", "Item 3"),
      selectedItem = null,
      onItemSelect = {},
      query = "",
      onQueryChange = {},
      itemLabeler = { it },
      leadingIcon = { Icon(painterResource(R.drawable.location_on_24px), null) }
    )
  }
}
