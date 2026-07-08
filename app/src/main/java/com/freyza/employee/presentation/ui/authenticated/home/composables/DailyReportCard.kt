package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.util.toCurrencyString
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.Visit
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
import kotlin.math.max

@Composable
fun DailyReportCard(
  dailyReport: DailyReport?,
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  val latestVisits = remember(dailyReport?.visits) {
    derivedStateOf {
      dailyReport?.visits?.take(Constants.NUM_VISITS_DAILY_REPORT_CARD)?.reversed() ?: listOf()
    }
  }

  val remainingVisits = remember(dailyReport?.visits) {
    max((dailyReport?.visits?.size ?: 0) - Constants.NUM_VISITS_DAILY_REPORT_CARD, 0)
  }

  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .padding(dimensionResource(R.dimen.default_spacing).times(4))
        .animateContentSize(),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing)),
    ) {
      // Header & Lock Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          "Daily Report".uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary
        )

        // show a locked or not locked badge if dailyReport exists and is of WORK dayType
        if (dailyReport != null && dailyReport.dayType == DayType.WORK) {
          ReportLockedBadge(dailyReport.locked)
        }
      }

      if (dailyReport == null) {
        Text("No Daily Report created for today", style = MaterialTheme.typography.bodyMedium)
        return@Column
      }

      // Day Type & Expense
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = dailyReport.dayType.name.uppercase(),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Medium
        )

        Text(
          text = dailyReport.totalExpense.toCurrencyString(),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary
        )
      }

      // Route Information
      if (dailyReport.dayType == DayType.WORK) {
        RouteItem(route)
      }

      when (dailyReport.dayType) {
        DayType.WORK -> WorkStatusContent(
          dailyReport = dailyReport,
          latestVisits = latestVisits.value,
          remainingVisits = remainingVisits
        )

        DayType.HOLIDAY -> {
          Text(
            "Enjoy the day!", style = MaterialTheme.typography.bodyMedium
          )
        }

        DayType.LEAVE -> {
          Text(
            "Enjoy your day off!", style = MaterialTheme.typography.bodyMedium
          )
        }
      }

      if (dailyReport.visits.isNotEmpty()) {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.default_spacing)),
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).times(1)
          )
        ) {
          val doctors = dailyReport.visits.count { it.visitType == VisitType.DOCTOR }
          val chemists = dailyReport.visits.count { it.visitType == VisitType.CHEMIST }
          val stockists = dailyReport.visits.count { it.visitType == VisitType.STOCKIST }

          if (doctors > 0) VisitBadge(count = doctors, type = VisitType.DOCTOR)
          if (chemists > 0) VisitBadge(count = chemists, type = VisitType.CHEMIST)
          if (stockists > 0) VisitBadge(count = stockists, type = VisitType.STOCKIST)
        }
      }
    }
  }
}

@Composable
private fun WorkStatusContent(
  dailyReport: DailyReport,
  latestVisits: List<Visit>,
  remainingVisits: Int,
  modifier: Modifier = Modifier,
) {
  if (dailyReport.visits.isEmpty()) {
    Text(
      "No visits recorded. Go to the reports tab to add one.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.secondary
    )

    return
  }

  Column(
    modifier = modifier.padding(top = dimensionResource(R.dimen.default_spacing)),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing).div(2))
  ) {
    latestVisits.forEachIndexed { index, visit ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing), Alignment.CenterHorizontally
        ), verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(visit.visitType.iconResource()),
          contentDescription = visit.visitType.titleCase(),
          modifier = Modifier.size(14.dp),
          tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
          when (visit.visitType) {
            VisitType.DOCTOR -> (visit as Visit.DoctorVisit).doctorName
            VisitType.STOCKIST -> (visit as Visit.StockistVisit).stockistName
            VisitType.CHEMIST -> (visit as Visit.ChemistVisit).chemistName
          }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    if (remainingVisits > 0) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = dimensionResource(R.dimen.default_spacing).div(2))
      ) {
        Icon(
          painter = painterResource(R.drawable.add_24px),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(14.dp)
        )
        Text(
          "$remainingVisits more",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary,
        )
      }
    }
  }
}

@Composable
fun DailyReportCardSkeleton(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    elevation = CardDefaults.outlinedCardElevation(),
    colors = CardDefaults.outlinedCardColors(),
    shape = RoundedCornerShape(size = integerResource(R.integer.rounding_radius).dp),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start,
      modifier = modifier
        .padding(
          vertical = dimensionResource(R.dimen.default_spacing).times(3),
          horizontal = dimensionResource(R.dimen.default_spacing).times(4)
        )
        .animateContentSize()
    ) {
      Text(
        "Daily Report Loading".uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary
      )
      Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

      Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
      ) {
        Skeleton(
          Modifier
            .width(64.dp)
            .height(28.dp)
        )
      }

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
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportCardWorkPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DailyReportCard(dummyDailyReportWork(), dummyRouteWithLocation())
      DailyReportCard(dummyDailyReportWork(true), dummyRouteWithLocation())
      DailyReportCard(dummyDailyReportWork(false, noVisits = true), dummyRouteWithLocation())
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportCardHolidayPreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(dummyDailyReportHoliday(), null)
  }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun DailyReportCardLeavePreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(dummyDailyReportLeave(), null)
  }
}


@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportCardNullPreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(null, null)
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportCardSkeletonPreview() {
  FreyzaEmployeeTheme {
    DailyReportCardSkeleton()
  }
}
