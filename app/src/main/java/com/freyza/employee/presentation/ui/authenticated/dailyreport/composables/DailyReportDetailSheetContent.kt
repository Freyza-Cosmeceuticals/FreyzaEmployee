package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.domain.model.routeName
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DailyReportDetailSheetContent(
  report: DailyReport?,
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
) {
  if (report == null) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.screen_padding).times(2)),
      contentAlignment = Alignment.Center
    ) {
      Text("No report selected", style = MaterialTheme.typography.bodyLarge)
    }
    return
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = dimensionResource(R.dimen.screen_padding))
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = DateFormatter.format(report.date),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = report.dayType.name,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.primary
        )

        if (report.dayType == DayType.WORK) {
          Spacer(Modifier.size(dimensionResource(R.dimen.default_spacing).times(2)))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              painter = painterResource(R.drawable.route_24px),
              contentDescription = null,
              modifier = Modifier.size(dimensionResource(R.dimen.default_spacing).times(4)),
              tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.default_spacing)))
            Text(
              text = if (!route?.routeName().isNullOrBlank()) route.routeName() else "No Route",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      if (report.dayType == DayType.WORK) {
        Badge(containerColor = if (report.locked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer) {
          Text(
            (if (report.locked) "Locked" else "Not Locked").uppercase(),
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(4)))

    ExpensesSummaryCard(ta = report.ta, da = report.da, total = report.totalExpense)
    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.default_spacing).times(4)))

    if (report.dayType == DayType.WORK) {
      Text(
        text = "Visits (${report.visits.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.screen_padding).div(2))
      )

      if (report.visits.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.screen_padding).times(2)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No visits recorded for this day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(
              3
            )
          ), modifier = Modifier.weight(1f, fill = false)
        ) {
          items(report.visits, key = { it.id }) { visit ->
            VisitListItem(visit)
          }
        }
      }
    }
  }
}

@Composable
private fun ExpensesSummaryCard(ta: Double?, da: Double?, total: Double?) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.screen_padding)),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      ExpenseItem("TA", ta)
      ExpenseItem("DA", da)
      ExpenseItem("Total", total)
    }
  }
}

@Composable
private fun ExpenseItem(label: String, amount: Double?) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label.uppercase(),
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = if (amount != null) "₹${amount}" else "-",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun VisitListItem(visit: Visit) {
  Row(
    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
  ) {
    val (icon, bgColor) = when (visit.visitType) {
      VisitType.DOCTOR -> painterResource(R.drawable.mail_24px) to MaterialTheme.colorScheme.primaryContainer
      VisitType.STOCKIST -> painterResource(R.drawable.mail_24px) to MaterialTheme.colorScheme.tertiaryContainer
      VisitType.CHEMIST -> painterResource(R.drawable.mail_24px) to MaterialTheme.colorScheme.errorContainer
    }

    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(bgColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = icon,
        contentDescription = visit.visitType.titleCase(),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.default_spacing).times(4)))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = visit.visitType.titleCase(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = "More info about the visit here",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = DateFormatter.format(visit.createdAt.toLocalDateTime(TimeZone.of(Constants.TIMEZONE)).time),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ReportDetailSheetContentPreviewWork() {
  FreyzaEmployeeTheme {
    DailyReportDetailSheetContent(dummyDailyReportWork(dateNow = true), dummyRouteWithLocation())
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ReportDetailSheetContentPreviewWorkNoVisits() {
  FreyzaEmployeeTheme {
    DailyReportDetailSheetContent(dummyDailyReportWork(noVisits = true), dummyRouteWithLocation())
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ReportDetailSheetContentPreviewLeave() {
  FreyzaEmployeeTheme {
    DailyReportDetailSheetContent(dummyDailyReportLeave(), dummyRouteWithLocation())
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ReportDetailSheetContentPreviewHoliday() {
  FreyzaEmployeeTheme {
    DailyReportDetailSheetContent(dummyDailyReportHoliday(), dummyRouteWithLocation())
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun ReportDetailSheetContentPreviewNull() {
  FreyzaEmployeeTheme {
    DailyReportDetailSheetContent(null, dummyRouteWithLocation())
  }
}
