package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun VisitBadge(count: Int, type: VisitType, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.background(
      MaterialTheme.colorScheme.tertiaryContainer,
      RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp)
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(
        horizontal = dimensionResource(R.dimen.default_spacing).times(3),
        vertical = dimensionResource(R.dimen.default_spacing)
      )
    ) {
      Icon(
        painter = painterResource(type.iconResource()),
        contentDescription = "${type.titleCase()} = $count",
        modifier = Modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.onTertiaryContainer
      )
      Spacer(Modifier.width(dimensionResource(R.dimen.default_spacing)))
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onTertiaryContainer
      )
    }
  }
}

@Preview
@Composable
private fun VisitBadgePreview() {
  FreyzaEmployeeTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      VisitBadge(5, VisitType.DOCTOR)
      VisitBadge(2, VisitType.DOCTOR)
      VisitBadge(7, VisitType.CHEMIST)
    }
  }

}
