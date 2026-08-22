package com.freyza.employee.presentation.ui.authenticated.addvisit.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.util.CurrencyFormatter
import com.freyza.employee.domain.model.PointOfInterest
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.composables.SearchableDropdown
import com.freyza.employee.presentation.ui.state.FormField
import com.freyza.employee.presentation.ui.state.ProductEntry

@Composable
fun ClientInfoCard(
  visitType: VisitType,
  name: FormField,
  onNameChange: (String) -> Unit,
  availablePois: List<PointOfInterest>,
  selectedPoiId: String?,
  onPoiSelect: (PointOfInterest?) -> Unit,
  samplesGiven: List<String>,
  samplesError: String?,
  onSamplesChange: (List<String>) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  ElevatedCard(modifier = modifier) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Client Details",
        style = MaterialTheme.typography.titleMedium,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
          alpha = 0.38f
        )
      )

      val label = when (visitType) {
        VisitType.DOCTOR -> "Doctor Name"
        VisitType.STOCKIST -> "Stockist Name"
        VisitType.CHEMIST -> "Chemist Name"
      }

      SearchableDropdown(
        label = label,
        items = availablePois,
        selectedItem = availablePois.find { it.id == selectedPoiId },
        onItemSelect = onPoiSelect,
        onQueryChange = onNameChange,
        query = name.value,
        itemLabeler = { poi ->
          if (poi.locationName != null) "${poi.name} (${poi.locationName})"
          else poi.name
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = "Search or enter name",
        leadingIcon = { Icon(painterResource(R.drawable.account_circle_24px), null) },
        enabled = enabled,
        isError = name.error != null,
        supportingText = name.error?.let { { Text(it) } }
      )

      if (visitType == VisitType.DOCTOR || visitType == VisitType.STOCKIST) {
        TagInputField(
          items = samplesGiven,
          onItemAdded = { if (!samplesGiven.contains(it)) onSamplesChange(samplesGiven + it) },
          onItemRemoved = { onSamplesChange(samplesGiven - it) },
          label = "Samples Given",
          enabled = enabled,
          error = samplesError
        )
      }
    }
  }
}

@Composable
fun OrderDetailsCard(
  visitType: VisitType,
  orderTaken: Boolean,
  onOrderTakenChange: (Boolean) -> Unit,
  productEntries: List<ProductEntry>,
  onProductUpdate: (Int, ProductEntry) -> Unit,
  onAddProduct: () -> Unit,
  onRemoveProduct: (Int) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Order Details",
        style = MaterialTheme.typography.titleMedium,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
          alpha = 0.38f
        )
      )

      ToggleableRow(
        checked = orderTaken,
        onCheckedChange = onOrderTakenChange,
        text = "Order Taken?",
        enabled = enabled
      )

      if (orderTaken && visitType == VisitType.DOCTOR) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          productEntries.forEachIndexed { index, product ->
            key(product._id) {
              ProductEntryRow(
                product = product,
                onProductUpdate = { onProductUpdate(index, it) },
                canRemove = productEntries.size > 1,
                onRemove = { onRemoveProduct(index) },
                focusManager = focusManager,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onAddProduct,
            enabled = enabled && productEntries.size < Constants.MAX_PRODUCT_ENTRIES
          ) {
            Text("+ Add product")
          }
        }
      }
    }
  }
}

@Composable
private fun ProductEntryRow(
  product: ProductEntry,
  onProductUpdate: (ProductEntry) -> Unit,
  canRemove: Boolean,
  onRemove: () -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // ROW 1: Name and remove button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = product.name.value,
          onValueChange = {
            onProductUpdate(
              product.copy(
                name = product.name.copy(
                  value = it,
                  error = null
                )
              )
            )
          },
          label = { Text("Product") },
          modifier = Modifier.weight(1.5f),
          singleLine = true,
          enabled = enabled,
          isError = product.name.error != null,
          supportingText = product.name.error?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        IconButton(onClick = onRemove, enabled = enabled && canRemove) {
          Icon(
            painter = painterResource(R.drawable.close_24px),
            contentDescription = "Remove",
            tint = if (enabled && canRemove) MaterialTheme.colorScheme.error else LocalContentColor.current.copy(
              alpha = if (enabled) 1f else 0.38f
            )
          )
        }
      }

      // ROW 2: Rate and Qty
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        OutlinedTextField(
          value = product.rate.value,
          onValueChange = {
            if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) {
              onProductUpdate(product.copy(rate = product.rate.copy(value = it, error = null)))
            }
          },
          prefix = { Text(CurrencyFormatter.symbol) },
          label = { Text("Rate") },
          modifier = Modifier.weight(1f),
          singleLine = true,
          enabled = enabled,
          isError = product.rate.error != null,
          supportingText = product.rate.error?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
          ),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        Spacer(modifier = Modifier.width(16.dp))

        QuantityStepper(
          quantity = product.quantity,
          onQuantityChange = { onProductUpdate(product.copy(quantity = it)) },
          enabled = enabled,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun QuantityStepper(
  quantity: FormField,
  onQuantityChange: (FormField) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val qty = quantity.value.toIntOrNull() ?: 1

  OutlinedTextField(
    value = quantity.value,
    onValueChange = { onQuantityChange(quantity.copy(value = it, error = null)) },
    enabled = enabled,
    leadingIcon = {
      // Minus Button
      IconButton(
        onClick = {
          if (qty > 1) onQuantityChange(
            quantity.copy(
              value = (qty - 1).toString(),
              error = null
            )
          )
        },
        modifier = Modifier.size(36.dp),
        enabled = enabled && qty > 1
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_left_24px), contentDescription = "Decrease"
        )
      }
    },
    trailingIcon = {
      // Plus Button
      IconButton(
        onClick = {
          if (qty < Constants.MAX_VISIT_PRODUCT_QUANTITY) onQuantityChange(
            quantity.copy(
              value = (qty + 1).toString(),
              error = null
            )
          )
        },
        modifier = Modifier.size(36.dp),
        enabled = enabled && qty < Constants.MAX_VISIT_PRODUCT_QUANTITY
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_right_24px), contentDescription = "Increase"
        )
      }
    },
    singleLine = true,
    isError = quantity.error != null,
    supportingText = quantity.error?.let { { Text(it) } },
    textStyle = MaterialTheme.typography.titleMedium.copy(
      fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
      color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.38f
      )
    ),
    label = { Text("Quantity") },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
    ),
    modifier = modifier
  )
}

@Composable
fun BillingDetailsCard(
  visitType: VisitType,
  outstandingAmount: FormField,
  onOutstandingAmountChange: (String) -> Unit,
  billNo: FormField,
  onBillNoChange: (String) -> Unit,
  stockChecked: Boolean,
  onStockCheckedChange: (Boolean) -> Unit,
  paymentCollected: Boolean,
  onPaymentCollectedChange: (Boolean) -> Unit,
  amountWithGST: FormField,
  onAmountWithGSTChange: (String) -> Unit,
  amountWithoutGST: FormField,
  onAmountWithoutGSTChange: (String) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Financials & Billing",
        style = MaterialTheme.typography.titleMedium,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
          alpha = 0.38f
        )
      )

      OutlinedTextField(
        value = outstandingAmount.value,
        onValueChange = {
          if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) {
            onOutstandingAmountChange(it)
          }
        },
        prefix = { Text(CurrencyFormatter.symbol) },
        label = { Text("Current Outstanding Amount") },
        isError = outstandingAmount.error != null,
        enabled = enabled,
        supportingText = outstandingAmount.error?.let { { Text(it) } },
        leadingIcon = { Icon(painterResource(R.drawable.account_balance_wallet_24px), null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
      )

      if (visitType == VisitType.STOCKIST) {
        OutlinedTextField(
          value = billNo.value,
          onValueChange = onBillNoChange,
          label = { Text("Bill Number") },
          isError = billNo.error != null,
          enabled = enabled,
          supportingText = billNo.error?.let { { Text(it) } },
          leadingIcon = { Icon(painterResource(R.drawable.receipt_24px), null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        ToggleableRow(
          checked = stockChecked,
          onCheckedChange = onStockCheckedChange,
          text = "Stock Checked?",
          enabled = enabled
        )
      }

      if (visitType == VisitType.STOCKIST || visitType == VisitType.CHEMIST) {
        ToggleableRow(
          checked = paymentCollected,
          onCheckedChange = onPaymentCollectedChange,
          text = "Payment Collected?",
          enabled = enabled
        )

        if (paymentCollected) {
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
              value = amountWithoutGST.value,
              onValueChange = {
                if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) {
                  onAmountWithoutGSTChange(it)
                }
              },
              prefix = { Text(CurrencyFormatter.symbol) },
              label = { Text("W/O GST") },
              isError = amountWithoutGST.error != null,
              enabled = enabled,
              supportingText = amountWithoutGST.error?.let { { Text(it) } },
              modifier = Modifier.weight(1f),
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
              ),
              keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )
            OutlinedTextField(
              value = amountWithGST.value,
              onValueChange = {
                if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) {
                  onAmountWithGSTChange(it)
                }
              },
              prefix = { Text(CurrencyFormatter.symbol) },
              label = { Text("With GST") },
              isError = amountWithGST.error != null,
              enabled = enabled,
              supportingText = amountWithGST.error?.let { { Text(it) } },
              modifier = Modifier.weight(1f),
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
              ),
              keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )
          }
        }
      }
    }
  }
}

@Composable
fun NotesCard(
  notes: FormField,
  onNotesChange: (String) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  ElevatedCard(modifier = modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        "Additional Info",
        style = MaterialTheme.typography.titleMedium,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
          alpha = 0.38f
        ),
        modifier = Modifier.padding(bottom = 16.dp)
      )

      OutlinedTextField(
        value = notes.value,
        onValueChange = onNotesChange,
        label = { Text("Notes") },
        isError = notes.error != null,
        enabled = enabled,
        supportingText = notes.error?.let { { Text(it) } },
        leadingIcon = { Icon(painterResource(R.drawable.description_24px), null) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
      )
    }
  }
}
