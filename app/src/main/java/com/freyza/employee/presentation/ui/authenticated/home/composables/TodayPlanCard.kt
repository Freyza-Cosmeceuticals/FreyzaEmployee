package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dummyTravelPlanEntry
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun TodayPlanCard(planEntry: TravelPlanEntry?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text("Travel Plan", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            if (planEntry == null) {
                Text("No travel plan for today", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    planEntry.dayType.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                )
            }

            when (planEntry.dayType) {
                DayType.WORK -> {
                    Text(
                        planEntry.routeId!!.uppercase().substring(0..5),
                        style = MaterialTheme.typography.bodyMedium
                    )
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
    }
}

@Preview
@Composable
fun TodayPlanCardPreview() {
    FreyzaEmployeeTheme {
        TodayPlanCard(
            planEntry = dummyTravelPlanEntry()
        )
    }
}

@Preview
@Composable
fun TodayPlanCardNullPreview() {
    FreyzaEmployeeTheme {
        TodayPlanCard(null)
    }
}
