package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.domain.model.DailyReport
import com.freyza.employee.domain.model.DayType

@Composable
fun DailyReportCard(dailyReport: UIState<DailyReport?>, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier
      .clickable {},
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
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          "Daily Report".uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

      when (dailyReport) {
        is UIState.Ready -> {
          val data = dailyReport.data

          if (data == null) {
            Text("No Daily Report created for today", style = MaterialTheme.typography.bodyMedium)
            return@Column
          }

          Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
          ) {
            Text(
              data.dayType.name.uppercase(),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            )
          }

          when (data.dayType) {
            DayType.WORK -> {
              Text(data.routeId.toString())
            }

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

        is UIState.Error -> {
          Text("Error Loading Daily Report")
        }

        is UIState.Loading -> {
          Text("Loading")
        }

        else -> {}
      }
    }
  }
}
