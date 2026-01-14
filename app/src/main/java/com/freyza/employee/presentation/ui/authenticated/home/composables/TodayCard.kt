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
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.core.util.toTitleCase
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

@Composable
fun TodayCard(today: LocalDateTime, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text("Today", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${today.day} ${today.month.name.toTitleCase()} ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.alignBy(FirstBaseline)
                )
                Text(
                    "${today.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignBy(FirstBaseline)
                )
            }

            Text(today.dayOfWeek.name.toTitleCase(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun TodayCardPreview() {
    FreyzaEmployeeTheme {
        TodayCard(
            today = LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 1,
                hour = 5,
                minute = 59,
                second = 59
            )
        )
    }
}
