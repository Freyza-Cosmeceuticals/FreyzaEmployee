package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.composables.ReportLockedBadge
import com.freyza.employee.presentation.ui.composables.RouteItem
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.composables.VisitBadge
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun DailyReportListCard(
  report: DailyReport?,
  route: RouteWithLocation?,
  travellingWith: User?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  isToday: Boolean = false,
) {
  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(
      width = 2.dp,
      color = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = 0.5f
      )
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 2.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier
        .background(
          if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
          else Color.Transparent
        )
        .padding(dimensionResource(R.dimen.default_spacing).times(4)),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing).times(2))
    ) {
      // Date & Lock Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (report != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (isToday) {
              Icon(
                painter = painterResource(R.drawable.today_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = if (isToday) "Today".uppercase() else DateFormatter.format(report.date),
              style = MaterialTheme.typography.labelLarge,
              color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
          }

          if (report.dayType == DayType.WORK) {
            ReportLockedBadge(report.locked)
          }
        }
      }

      if (report == null) {
        Text(stringResource(R.string.no_report), style = MaterialTheme.typography.bodyMedium)
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
          fontWeight = FontWeight.Bold
        )

        Text(
          text = report.totalExpense.toCurrencyString(),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Route Information
      if (report.dayType == DayType.WORK) {
        RouteItem(route)

        if (report.travellingWithId != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing))
          ) {
            Icon(
              painter = painterResource(R.drawable.account_circle_24px),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Travelling with ${travellingWith?.name ?: ""}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (travellingWith == null) {
              Skeleton(
                modifier = Modifier
                  .height(16.dp)
                  .width(32.dp)
              )
            }
          }
        }

        val counts = report.computedVisitCounts

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val visitCountText = if (counts.totalCount == 0) {
            if (isToday)
              stringResource(R.string.no_visits_hint_add)
            else
              stringResource(R.string.no_visits)
          } else {
            if (counts.totalCount == 1)
              "1 Visit"
            else
              "${counts.totalCount} Visits"
          }

          Text(
            visitCountText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (counts.totalCount > 0) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.default_spacing).times(
                  2
                )
              )
            ) {
              if (counts.doctorCount > 0) VisitBadge(
                count = counts.doctorCount,
                type = VisitType.DOCTOR
              )
              if (counts.chemistCount > 0) VisitBadge(
                count = counts.chemistCount,
                type = VisitType.CHEMIST
              )
              if (counts.stockistCount > 0) VisitBadge(
                count = counts.stockistCount,
                type = VisitType.STOCKIST
              )
            }
          }
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
          text = "${stringResource(R.string.daily_report_title)} Loading".uppercase(),
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
      DailyReportListCard(
        dummyDailyReportWork(),
        route = dummyRouteWithLocation(),
        travellingWith = dummyUserEmployee(),
        isToday = true
      )
      DailyReportListCard(
        dummyDailyReportWork(true),
        travellingWith = null,
        route = null
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardHolidayPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(
        dummyDailyReportHoliday(),
        travellingWith = dummyUserEmployee(),
        route = null,
        isToday = true
      )
      DailyReportListCard(dummyDailyReportHoliday(), travellingWith = null, route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardLeavePreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(
        dummyDailyReportLeave(),
        travellingWith = null,
        route = null,
        isToday = true
      )
      DailyReportListCard(dummyDailyReportLeave(), travellingWith = null, route = null)
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportListCardNullPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportListCard(null, travellingWith = null, route = null, isToday = true)
      DailyReportListCard(null, travellingWith = null, route = null)
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
