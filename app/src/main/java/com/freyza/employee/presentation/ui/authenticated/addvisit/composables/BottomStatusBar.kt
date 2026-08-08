package com.freyza.employee.presentation.ui.authenticated.addvisit.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.core.util.toMoney
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
internal fun BottomStatusBar(
  visitType: VisitType,
  orderTaken: Boolean,
  orderAmount: Money,
  numProducts: Int,
  totalQuantity: Int,
  creationState: UIState<Boolean>,
  onSubmitVisit: () -> Unit,
  modifier: Modifier = Modifier,
  buttonText: String = "Save Visit",
) {
  return Surface(
    tonalElevation = 8.dp,
    shadowElevation = 8.dp,
    modifier = modifier,
    shape = RoundedCornerShape(
      topStart = CornerSize(integerResource(R.integer.rounding_radius).dp),
      topEnd = CornerSize(integerResource(R.integer.rounding_radius).dp),
      bottomStart = ZeroCornerSize,
      bottomEnd = ZeroCornerSize
    )
  ) {
    Column(
      modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding))
    ) {
      if (visitType == VisitType.DOCTOR && orderTaken) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.default_spacing)),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Products:", style = MaterialTheme.typography.bodyLarge)
          Text(
            text = "$numProducts ($totalQuantity units)",
            style = MaterialTheme.typography.titleMedium
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.default_spacing)),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Order Total:", style = MaterialTheme.typography.bodyLarge)
          Text(
            text = orderAmount.toCurrencyString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (creationState is UIState.Error) {
        Text(
          creationState.message ?: "An error has occurred",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      SubmitVisitButton(
        onSubmitVisit = onSubmitVisit,
        creationState = creationState,
        modifier = Modifier.fillMaxWidth(),
        text = buttonText,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun BottomStatusBarPreview() {
  FreyzaEmployeeTheme {
    BottomStatusBar(
      visitType = VisitType.DOCTOR,
      orderTaken = true,
      orderAmount = "7833.23328".toMoney(),
      numProducts = 7,
      totalQuantity = 86,
      creationState = UIState.Idle(true),
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun BottomStatusBarChemistPreview() {
  FreyzaEmployeeTheme {
    BottomStatusBar(
      visitType = VisitType.CHEMIST,
      orderTaken = true,
      orderAmount = "7833.23328".toMoney(),
      numProducts = 4,
      totalQuantity = 86,
      creationState = UIState.Loading(),
      onSubmitVisit = {})
  }
}

@Preview(showBackground = true)
@Composable
private fun BottomStatusBarStockistPreview() {
  FreyzaEmployeeTheme {
    BottomStatusBar(
      visitType = VisitType.STOCKIST,
      orderTaken = true,
      orderAmount = "7833.23328".toMoney(),
      numProducts = 7,
      totalQuantity = 86,
      creationState = UIState.Error("Here is an error"),
      onSubmitVisit = {})
  }
}
