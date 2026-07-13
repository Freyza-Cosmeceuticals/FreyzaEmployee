package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Money
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.dummyVisitChemistAllTrue
import com.freyza.employee.domain.model.dummyVisitDoctorAllTrue
import com.freyza.employee.domain.model.dummyVisitStockistAllTrue
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun VisitListItem(visit: Visit, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
  ListItem(
    modifier = modifier
      .clip(RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp))
      .clickable(onClick = onClick),
    leadingContent = {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(visit.visitType.iconResource()),
          contentDescription = visit.visitType.titleCase(),
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
          modifier = Modifier.size(18.dp)
        )
      }
    },
    overlineContent = {
      Text(visit.visitType.name.uppercase())
    },
    headlineContent = {
      Text(
        text = when (visit) {
          is Visit.DoctorVisit -> visit.doctorName
          is Visit.StockistVisit -> visit.stockistName
          is Visit.ChemistVisit -> visit.chemistName
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
    },
    supportingContent = {
      Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing))) {
        Text(
          text = visit.additionalNotes.takeUnless { it.isNullOrBlank() }
            ?: "No additional information",
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing)),
          modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
          when (visit) {
            is Visit.DoctorVisit -> {
              visit.productDetails.takeIf { it.isNotEmpty() }
                ?.let { products -> InfoChip(text = "${products.size} products") }
              visit.samplesGiven.takeIf { it.isNotEmpty() }
                ?.let { samples -> InfoChip(text = "${samples.size} samples") }
              visit.orderTaken.takeIf { it }?.let {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  text = "Order: ${visit.orderAmount.toCurrencyString()}"
                )
              }
              if (visit.outstandingAmount > Money.ZERO) {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.errorContainer,
                  text = "Outstanding: ${visit.outstandingAmount.toCurrencyString()}"
                )
              }
            }

            is Visit.StockistVisit -> {
              visit.stockChecked.takeIf { it }?.let {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  text = "Stock Checked"
                )
              }
              visit.samplesGiven.takeIf { it.isNotEmpty() }
                ?.let { samples -> InfoChip(text = "${samples.size} samples") }
              visit.orderTaken.takeIf { it }?.let {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.primaryContainer, text = "Order taken"
                )
              }
              visit.paymentCollected.takeIf { it }?.let {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  text = "Payment Collected"
                )
              }
              if (visit.outstandingAmount > Money.ZERO) {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.errorContainer,
                  text = "Outstanding: ${visit.outstandingAmount.toCurrencyString()}"
                )
              }
            }

            is Visit.ChemistVisit -> {
              visit.orderTaken.takeIf { it }?.let {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.primaryContainer, text = "Order taken"
                )
              }
              if (visit.outstandingAmount > Money.ZERO) {
                InfoChip(
                  containerColor = MaterialTheme.colorScheme.errorContainer,
                  text = "Outstanding: ${visit.outstandingAmount.toCurrencyString()}"
                )
              }
            }
          }
        }
      }
    },
    trailingContent = {
      Text(
        text = DateFormatter.format(visit.createdAt.toLocalDateTime(TimeZone.of(Constants.TIMEZONE)).time),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    tonalElevation = 2.dp
  )
}

@JvmOverloads
@Composable
fun InfoChip(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
  contentColor: Color = contentColorFor(containerColor),
  content: @Composable () -> Unit,
) {
  Surface(
    shape = MaterialTheme.shapes.small,
    color = containerColor,
    contentColor = contentColor,
    modifier = modifier
  ) {
    content()
  }
}

@JvmOverloads
@Composable
fun InfoChip(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
  contentColor: Color = contentColorFor(containerColor),
  text: String,
) {
  InfoChip(modifier, containerColor, contentColor) {
    Text(
      text = text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(
        horizontal = dimensionResource(R.dimen.default_spacing).times(1.5f),
        vertical = dimensionResource(R.dimen.default_spacing).div(2)
      )
    )
  }
}

@Preview
@Composable
private fun InfoChipPreview() {
  FreyzaEmployeeTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      InfoChip(containerColor = MaterialTheme.colorScheme.tertiaryContainer) { Text("Hello") }
      InfoChip(text = "Hello")
    }
  }
}

@Preview
@Composable
private fun VisitListItemPreview() {
  FreyzaEmployeeTheme {
    Column {
      VisitListItem(dummyVisitDoctorAllTrue())
      VisitListItem(dummyVisitStockistAllTrue())
      VisitListItem(dummyVisitChemistAllTrue())
    }
  }
}
