package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.presentation.ui.composables.ReportLockedBadge
import com.freyza.employee.presentation.ui.composables.RouteItem
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.composables.VisitBadge
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun DailyReportListCard(
  report: DailyReport?,
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  isToday: Boolean = false,
) {
  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
      else MaterialTheme.colorScheme.surface
    ),
    border = if (isToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 2.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier.padding(dimensionResource(R.dimen.default_spacing).times(4)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing).times(2))
    ) {
      // Date & Lock Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (report != null) {
          Text(
            text = if (isToday) "Today".uppercase() else DateFormatter.format(report.date),
            style = MaterialTheme.typography.labelLarge,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
          )

          if (report.dayType == DayType.WORK) {
            ReportLockedBadge(report.locked)
          }
        }
      }

      if (report == null) {
        Text("No Daily Report", style = MaterialTheme.typography.bodyMedium)
        return@Column
      }

      // Day Type & Expense
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = report.dayType.name.uppercase(),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Medium
        )

        Text(
          text = "₹${report.totalExpense ?: 0.0}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
      }

      // Route Information
      if (report.dayType == DayType.WORK) {
        RouteItem(route)

        if (report.visits.isEmpty()) {
          Text(
            "No visits logged." + if (isToday) " Click Add Visit to add one" else "",
            style = MaterialTheme.typography.bodySmall
          )
        } else {
          Text(
            "${report.visits.size} Visits logged", style = MaterialTheme.typography.bodySmall
          )
        }
      }

      if (report.visits.isNotEmpty()) {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Row(
          modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(3)
          )
        ) {
          val doctors = report.visits.count { it.visitType == VisitType.DOCTOR }
          val chemists = report.visits.count { it.visitType == VisitType.CHEMIST }
          val stockists = report.visits.count { it.visitType == VisitType.STOCKIST }

          if (doctors > 0) VisitBadge(count = doctors, type = VisitType.DOCTOR)
          if (chemists > 0) VisitBadge(count = chemists, type = VisitType.CHEMIST)
          if (stockists > 0) VisitBadge(count = stockists, type = VisitType.STOCKIST)
        }
      }
    }
  }
}

@Composable
fun DailyReportListCardSkeleton(modifier: Modifier = Modifier, isToday: Boolean = false) {
  Card(
    onClick = {},
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
      else MaterialTheme.colorScheme.surface
    ),
    border = if (isToday) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 2.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier.padding(dimensionResource(R.dimen.default_spacing).times(4)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing).times(2))
    ) {
      // Date & Lock Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Daily Report Loading".uppercase(),
          style = MaterialTheme.typography.labelLarge,
          color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Skeleton(
          modifier = Modifier
            .width(48.dp)
            .height(20.dp)
        )

        Skeleton(
          modifier = Modifier
            .width(48.dp)
            .height(20.dp)
        )
      }

      // Route Information
      Skeleton(
        modifier = Modifier
          .width(64.dp)
          .height(16.dp)
      )

      Skeleton(
        modifier = Modifier
          .width(72.dp)
          .height(16.dp)
      )

      HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

      Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(3)
        )
      ) {
        Skeleton(
          modifier = Modifier
            .width(24.dp)
            .height(16.dp)
        )

        Skeleton(
          modifier = Modifier
            .width(24.dp)
            .height(16.dp)
        )

        Skeleton(
          modifier = Modifier
            .width(24.dp)
            .height(16.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardWorkPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(dummyDailyReportWork(), route = dummyRouteWithLocation(), isToday = true)
      DailyReportListCard(dummyDailyReportWork(true), route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardHolidayPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(dummyDailyReportHoliday(), route = null, isToday = true)
      DailyReportListCard(dummyDailyReportHoliday(), route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardLeavePreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(dummyDailyReportLeave(), route = null, isToday = true)
      DailyReportListCard(dummyDailyReportLeave(), route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardNullPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(null, route = null, isToday = true)
      DailyReportListCard(null, route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardSkeletonPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCardSkeleton(isToday = true)
      DailyReportListCardSkeleton()
    }
  }
}
