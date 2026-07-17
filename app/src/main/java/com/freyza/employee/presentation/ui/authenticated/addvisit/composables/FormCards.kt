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
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.state.AddVisitFormState
import com.freyza.employee.presentation.ui.state.ProductEntry

@Composable
fun ClientInfoCard(
  visitType: VisitType,
  form: AddVisitFormState,
  onFormUpdate: (AddVisitFormState) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Client Details",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )

      val (nameValue, onNameChange, label) = when (visitType) {
        VisitType.DOCTOR -> Triple(
          form.doctorName, { it: String -> onFormUpdate(form.copy(doctorName = it)) }, "Doctor Name"
        )

        VisitType.STOCKIST -> Triple(
          form.stockistName,
          { it: String -> onFormUpdate(form.copy(stockistName = it)) },
          "Stockist Name"
        )

        VisitType.CHEMIST -> Triple(
          form.chemistName,
          { it: String -> onFormUpdate(form.copy(chemistName = it)) },
          "Chemist Name"
        )
      }

      OutlinedTextField(
        value = nameValue,
        onValueChange = onNameChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(painterResource(R.drawable.account_circle_24px), null) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
      )

      if (visitType == VisitType.DOCTOR || visitType == VisitType.STOCKIST) {
        TagInputField(
          items = form.samplesGiven,
          onItemAdded = { if (!form.samplesGiven.contains(it)) onFormUpdate(form.copy(samplesGiven = form.samplesGiven + it)) },
          onItemRemoved = { onFormUpdate(form.copy(samplesGiven = form.samplesGiven - it)) },
          label = "Samples Given"
        )
      }
    }
  }
}

@Composable
fun OrderDetailsCard(
  visitType: VisitType,
  form: AddVisitFormState,
  onFormUpdate: (AddVisitFormState) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Order Details",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )

      ToggleableRow(
        checked = form.orderTaken,
        onCheckedChange = { onFormUpdate(form.copy(orderTaken = it)) },
        text = "Order Taken?"
      )

      if (form.orderTaken && visitType == VisitType.DOCTOR) {
        // We use a Column with keys instead of LazyColumn items since it's nested
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          form.productEntries.forEachIndexed { index, product ->
            key(product.id) {
              ProductEntryRow(
                product = product, onProductUpdate = { updatedProduct ->
                val newList =
                  form.productEntries.toMutableList().apply { set(index, updatedProduct) }
                onFormUpdate(form.copy(productEntries = newList))
              }, canRemove = form.productEntries.size > 1, onRemove = {
                val newList = form.productEntries.toMutableList().apply { removeAt(index) }
                onFormUpdate(form.copy(productEntries = newList))
              }, focusManager = focusManager, modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = {
            if (form.productEntries.size < Constants.MAX_PRODUCT_ENTRIES) onFormUpdate(
              form.copy(productEntries = form.productEntries + ProductEntry())
            )
          }, enabled = form.productEntries.size < Constants.MAX_PRODUCT_ENTRIES) {
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
          value = product.name,
          onValueChange = { onProductUpdate(product.copy(name = it)) },
          label = { Text("Product") },
          modifier = Modifier.weight(1.5f),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        IconButton(onClick = onRemove, enabled = canRemove) {
          Icon(
            painter = painterResource(R.drawable.close_24px),
            contentDescription = "Remove",
            tint = if (canRemove) MaterialTheme.colorScheme.error else LocalContentColor.current
          )
        }
      }

      // ROW 2: Rate and Qty
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        OutlinedTextField(
          value = product.rate,
          onValueChange = {
            if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) {
              onProductUpdate(product.copy(rate = it))
            }
          },
          prefix = { Text(CurrencyFormatter.symbol) },
          label = { Text("Rate") },
          modifier = Modifier.weight(1f),
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
          ),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        Spacer(modifier = Modifier.width(16.dp))

        QuantityStepper(
          quantity = product.quantity,
          onQuantityChange = { onProductUpdate(product.copy(quantity = it)) },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun QuantityStepper(
  quantity: String,
  onQuantityChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val qty = quantity.toIntOrNull() ?: 1

  OutlinedTextField(
    value = qty.toString(),
    onValueChange = { onQuantityChange(it) },
    leadingIcon = {
      // Minus Button
      IconButton(
        onClick = { if (qty > 1) onQuantityChange((qty - 1).toString()) },
        modifier = Modifier.size(36.dp),
        enabled = qty > 1
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_left_24px), contentDescription = "Decrease"
        )
      }
    },
    trailingIcon = {
      // Plus Button
      IconButton(
        onClick = { if (qty < Constants.MAX_VISIT_PRODUCT_QUANTITY) onQuantityChange((qty + 1).toString()) },
        modifier = Modifier.size(36.dp),
        enabled = qty < Constants.MAX_VISIT_PRODUCT_QUANTITY
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_right_24px), contentDescription = "Increase"
        )
      }
    },
    singleLine = true,
    textStyle = MaterialTheme.typography.titleMedium.copy(
      fontWeight = FontWeight.Medium, textAlign = TextAlign.Center
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
  form: AddVisitFormState,
  onFormUpdate: (AddVisitFormState) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        "Financials & Billing",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )

      OutlinedTextField(
        value = form.outstandingAmount,
        onValueChange = {
          // UX Rule: Only allow digits, and only allow ONE decimal point max
          if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) onFormUpdate(
            form.copy(outstandingAmount = it)
          )
        },
        prefix = { Text(CurrencyFormatter.symbol) },
        label = { Text("Current Outstanding Amount") },
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
          value = form.billNo,
          onValueChange = { onFormUpdate(form.copy(billNo = it)) },
          label = { Text("Bill Number") },
          leadingIcon = { Icon(painterResource(R.drawable.receipt_24px), null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )

        ToggleableRow(
          checked = form.stockChecked,
          onCheckedChange = { onFormUpdate(form.copy(stockChecked = it)) },
          text = "Stock Checked?"
        )
      }

      if (visitType == VisitType.STOCKIST || visitType == VisitType.CHEMIST) {
        ToggleableRow(
          checked = form.paymentCollected,
          onCheckedChange = { onFormUpdate(form.copy(paymentCollected = it)) },
          text = "Payment Collected?"
        )

        if (form.paymentCollected) {
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
              value = form.amountWithoutGST,
              onValueChange = {
                // only allow digits, and only allow ONE decimal point max
                if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) onFormUpdate(
                  form.copy(amountWithoutGST = it)
                )
              },
              prefix = { Text(CurrencyFormatter.symbol) },
              label = { Text("W/O GST") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
              ),
              keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )
            OutlinedTextField(
              value = form.amountWithGST,
              onValueChange = {
                if (it.isEmpty() || (it.count { c -> c == '.' } <= 1 && it.all { c -> c.isDigit() || c == '.' })) onFormUpdate(
                  form.copy(amountWithGST = it)
                )
              },
              prefix = { Text(CurrencyFormatter.symbol) },
              label = { Text("With GST") },
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
  notes: String,
  onNotesChange: (String) -> Unit,
  focusManager: FocusManager,
  modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        "Additional Info",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Notes") },
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
