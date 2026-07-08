package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import com.freyza.employee.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
  label: String,
  items: List<T>,
  selectedItem: T?,
  onItemSelect: (T?) -> Unit,
  itemLabeler: (T) -> String,
  modifier: Modifier = Modifier,
  placeholder: String = "Select option",
) {
  var expanded by remember { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf(selectedItem?.let(itemLabeler) ?: "") }

  LaunchedEffect(selectedItem) {
    val label = selectedItem?.let(itemLabeler) ?: ""
    if (searchQuery != label) {
      searchQuery = label
    }
  }

  val filteredItems = remember(searchQuery, items) {
    if (searchQuery.isEmpty()) {
      items
    } else {
      items.filter { itemLabeler(it).contains(searchQuery, ignoreCase = true) }
    }
  }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    modifier = modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = {
        searchQuery = it
        expanded = true
        val matchedItem = items.find { item -> itemLabeler(item).equals(it, ignoreCase = true) }
        if (matchedItem != selectedItem) {
          onItemSelect(matchedItem)
        }
      },
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      modifier = Modifier
        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true)
        .fillMaxWidth(),
      shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
      singleLine = true
    )

    if (filteredItems.isNotEmpty()) {
      ExposedDropdownMenu(
        expanded = expanded, onDismissRequest = { expanded = false }) {
        filteredItems.forEach { item ->
          DropdownMenuItem(
            text = { Text(itemLabeler(item)) }, onClick = {
              onItemSelect(item)
              searchQuery = itemLabeler(item)
              expanded = false
            }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
          )
        }
      }
    }
  }
}
