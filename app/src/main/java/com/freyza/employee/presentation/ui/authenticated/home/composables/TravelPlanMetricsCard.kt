package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.core.util.toMoney
import com.freyza.employee.domain.model.TravelPlanMetrics
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun TravelPlanMetricsCard(
  metrics: TravelPlanMetrics?,
  monthName: String,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  daysLeft: Int? = null,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = monthName.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold
      )

      if (daysLeft != null) {
        Text(
          text = if (daysLeft == 1) "1 day left" else "$daysLeft days left",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.outline,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    if (metrics == null && isLoading) {
      MetricsSkeleton()
    } else if (metrics != null) {
      val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.5f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "metricsAlphaAnimation"
      )

      Box(modifier = Modifier.graphicsLayer { alpha = contentAlpha }) {
        MetricsContent(metrics)
      }
    } else {
      Text(
        text = "No travel plan targets found for this month.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun MetricsContent(metrics: TravelPlanMetrics) {
  var hasAppeared by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    hasAppeared = true
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    val targetProgress = if (hasAppeared) (metrics.percentage / 100f).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
      targetValue = targetProgress,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      label = "metricsProgressAnimation"
    )

    val targetAmount = if (hasAppeared) metrics.currentAmount.toFloat() else 0f
    val animatedAmount by animateFloatAsState(
      targetValue = targetAmount,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      label = "metricsAmountAnimation"
    )

    val targetPercentage = if (hasAppeared) metrics.percentage else 0f
    val animatedPercentage by animateFloatAsState(
      targetValue = targetPercentage,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      label = "metricsPercentageAnimation"
    )

    val targetColor = when {
      metrics.percentage < 15 -> Color(0xFFE57373) // Red
      metrics.percentage < 50 -> MaterialTheme.colorScheme.primary
      metrics.percentage < 85 -> Color(0xFF64B5F6) // Blue
      metrics.percentage < 100 -> Color(0xFF81C784) // Green
      else -> Color(0xFFFFD700) // Golden
    }

    val animatedColor by animateColorAsState(
      targetValue = targetColor,
      animationSpec = tween(durationMillis = 800),
      label = "metricsColorAnimation"
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row {
          Text(
            text = animatedAmount.toDouble().toMoney().toCurrencyString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            " / ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            metrics.targetAmount.toMoney().toCurrencyString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Text(
        text = "${animatedPercentage.toInt()}%",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(3)))

    LinearProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(CircleShape),
      color = animatedColor,
      trackColor = animatedColor.copy(alpha = 0.25f)
    )

    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    val visitsStr = if (metrics.numVisits == 1) "1 visit" else "${metrics.numVisits} visits"
    val reportsStr = if (metrics.numReports == 1) "1 report" else "${metrics.numReports} reports"

    Text(
      text = "Achieved in $visitsStr across $reportsStr",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun MetricsSkeleton() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Skeleton(
      modifier = Modifier
        .width(150.dp)
        .height(24.dp)
    )

    Skeleton(
      modifier = Modifier
        .width(40.dp)
        .height(24.dp)
    )
  }

  Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

  Skeleton(
    modifier = Modifier
      .fillMaxWidth()
      .height(12.dp)
  )
}

@Preview(showBackground = true)
@Composable
private fun TravelPlanMetricsCardPreview() {
  FreyzaEmployeeTheme {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      // Red (< 15%)
      TravelPlanMetricsCard(
        metrics = TravelPlanMetrics(
          targetAmount = 10000.0,
          employeeId = "1",
          totalOrderAmount = 500.0,
          totalAmountWithoutGST = 500.0,
          numReports = 2,
          numVisits = 4
        ), // 10%
        monthName = "August",
        daysLeft = 25,
        isLoading = false
      )

      // Primary (15% - 49%)
      TravelPlanMetricsCard(
        metrics = TravelPlanMetrics(
          targetAmount = 10000.0,
          employeeId = "1",
          totalOrderAmount = 1500.0,
          totalAmountWithoutGST = 1500.0,
          numReports = 5,
          numVisits = 12
        ), // 30%
        monthName = "August",
        daysLeft = 18,
        isLoading = false
      )

      // Blue (50% - 84%)
      TravelPlanMetricsCard(
        metrics = TravelPlanMetrics(
          targetAmount = 10000.0,
          employeeId = "1",
          totalOrderAmount = 3000.0,
          totalAmountWithoutGST = 3000.0,
          numReports = 10,
          numVisits = 25
        ), // 60%
        monthName = "August",
        daysLeft = 10,
        isLoading = false
      )

      // Green (85% - 99%)
      TravelPlanMetricsCard(
        metrics = TravelPlanMetrics(
          targetAmount = 10000.0,
          employeeId = "1",
          totalOrderAmount = 4500.0,
          totalAmountWithoutGST = 4500.0,
          numReports = 15,
          numVisits = 38
        ), // 90%
        monthName = "August",
        daysLeft = 3,
        isLoading = false
      )

      // Golden (>= 100%)
      TravelPlanMetricsCard(
        metrics = TravelPlanMetrics(
          targetAmount = 10000.0,
          employeeId = "1",
          totalOrderAmount = 6000.0,
          totalAmountWithoutGST = 5000.0,
          numReports = 18,
          numVisits = 45
        ), // 110%
        monthName = "August",
        daysLeft = 1,
        isLoading = false
      )

      // Loading
      TravelPlanMetricsCard(
        metrics = null,
        monthName = "August",
        daysLeft = 12,
        isLoading = true
      )
    }
  }
}
