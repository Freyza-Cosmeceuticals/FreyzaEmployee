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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Visit
import com.freyza.employee.domain.model.dummyDailyReportHoliday
import com.freyza.employee.domain.model.dummyDailyReportLeave
import com.freyza.employee.domain.model.dummyDailyReportWork
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun DailyReportCard(
  dailyReport: DailyReport?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  val latestVisits = remember(dailyReport?.visits) {
    derivedStateOf {
      dailyReport?.visits?.take(Constants.NUM_VISITS_DAILY_REPORT_CARD)?.reversed() ?: listOf()
    }
  }

  val moreVisits = remember(dailyReport?.visits) {
    (dailyReport?.visits?.size ?: 0) > Constants.NUM_VISITS_DAILY_REPORT_CARD
  }

  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
        alpha = 0.5f
      )
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
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
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          "Daily Report".uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary
        )

        // show a locked or not locked badge if dailyReport exists and is of WORK dayType
        if (dailyReport != null && dailyReport.dayType == DayType.WORK) {
          if (dailyReport.locked) {
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
              Text(
                "Locked".uppercase(), style = MaterialTheme.typography.labelMedium
              )
            }
          } else {
            Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
              Text(
                "Not Locked".uppercase(), style = MaterialTheme.typography.labelMedium
              )
            }
          }
        }
      }

      Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

      if (dailyReport == null) {
        Text("No Daily Report created for today", style = MaterialTheme.typography.bodyMedium)
        return@Column
      }

      Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
      ) {
        Text(
          dailyReport.dayType.name.uppercase(),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
        )
      }

      when (dailyReport.dayType) {
        DayType.WORK -> WorkStatusContent(
          dailyReport = dailyReport, latestVisits = latestVisits.value, moreVisits = moreVisits
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
    }
  }
}

@Composable
private fun WorkStatusContent(
  dailyReport: DailyReport,
  latestVisits: List<Visit>,
  moreVisits: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.padding(vertical = dimensionResource(R.dimen.default_spacing).times(1))) {
    if (dailyReport.visits.isNotEmpty()) {
      Text(
        "${dailyReport.visits.size} Visits logged",
        style = MaterialTheme.typography.bodyMedium,
      )

      latestVisits.forEachIndexed { index, visit ->
        Text("${index + 1}. ${visit.id}", style = MaterialTheme.typography.bodyMedium)
      }

      if (moreVisits) {
        Text("...")
      }
    } else {
      Text(
        "No visits logged yet. Open the report and add some visits.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary
      )
    }

    Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

    Text(
      "Total Expense: ₹${dailyReport.totalExpense ?: 0.0}",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.tertiary,
      fontWeight = FontWeight.ExtraBold
    )
  }
}

@Composable
fun DailyReportCardSkeleton(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    elevation = CardDefaults.outlinedCardElevation(),
    colors = CardDefaults.outlinedCardColors(),
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
      DailyReportCard(dummyDailyReportWork())
      DailyReportCard(dummyDailyReportWork(true))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DailyReportCardHolidayPreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(dummyDailyReportHoliday())
  }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun DailyReportCardLeavePreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(dummyDailyReportLeave())
  }
}


@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportCardNullPreview() {
  FreyzaEmployeeTheme {
    DailyReportCard(null)
  }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
private fun DailyReportCardSkeletonPreview() {
  FreyzaEmployeeTheme {
    DailyReportCardSkeleton()
  }
}
