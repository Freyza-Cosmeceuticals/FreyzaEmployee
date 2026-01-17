package com.freyza.employee.presentation.ui.authenticated.home.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freyza.employee.core.util.toPx
import com.freyza.employee.core.util.toTitleCase
import com.freyza.employee.domain.model.DayType
import com.freyza.employee.domain.model.Location
import com.freyza.employee.domain.model.Route
import com.freyza.employee.domain.model.TravelPlanEntry
import com.freyza.employee.domain.model.dummyLocation
import com.freyza.employee.domain.model.dummyLocationAlt
import com.freyza.employee.domain.model.dummyRoute
import com.freyza.employee.domain.model.dummyTravelPlanEntryHoliday
import com.freyza.employee.domain.model.dummyTravelPlanEntryLeave
import com.freyza.employee.domain.model.dummyTravelPlanEntryWork
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun TodayPlanCard(
    planEntry: TravelPlanEntry?,
    route: Route?,
    srcDestPair: Pair<Location, Location>?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {},
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                )
            }

            when (planEntry.dayType) {
                DayType.WORK -> {
                    Box(contentAlignment = Alignment.CenterEnd) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                12.dp, Alignment.CenterVertically
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                srcDestPair?.first?.name?.toTitleCase() ?: "???",
                                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                srcDestPair?.second?.name?.toTitleCase() ?: "???",
                                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Start),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        RouteArrow(
                            srcText = srcDestPair?.first?.name ?: "???",
                            destText = srcDestPair?.second?.name ?: "???",
                            padding = 8.dp.toPx(),
                            strokeWidth = 3.dp
                        )

                        Text(
                            "${route?.distanceKm?.toInt() ?: "???"}km".uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.padding(end = 18.dp)
                        )
                    }
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

@Composable
fun RouteArrow(
    srcText: String,
    destText: String,
    padding: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    lineColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    // Create a text measurer for dynamic text measurement
    val textMeasurer = rememberTextMeasurer()
    val srcTextResult = textMeasurer.measure(text = srcText)
    val destTextResult = textMeasurer.measure(text = destText)

    Canvas(modifier = modifier.fillMaxWidth()) {
        val dashOnInterval1 = (strokeWidth * 4).toPx()
        val dashOffInterval1 = (strokeWidth * 2).toPx()
        val dashOnInterval2 = (strokeWidth / 4).toPx()
        val dashOffInterval2 = (strokeWidth * 2).toPx()

        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashOnInterval1, dashOffInterval1, dashOnInterval2, dashOffInterval2), 0f
        )

        val srcEndX = srcTextResult.size.width.toFloat() + padding * 1.5f
        val srcEndY = -srcTextResult.size.height.toFloat()

        val destEndX = destTextResult.size.width.toFloat() + padding * 2.0f
        val destEndY = destTextResult.size.height.toFloat()

        val turnX = size.width - padding
        val curveRadius = 40f   // increase for smoother / wider curve


        val path = Path().apply {
            moveTo(srcEndX, srcEndY)
            lineTo(turnX - curveRadius, srcEndY)

            // Curve out to the right
            cubicTo(
                turnX, srcEndY,                 // control 1: keep horizontal direction
                turnX, destEndY,                // control 2: pull curve vertically
                turnX - curveRadius, destEndY   // curve end
            )

            lineTo(destEndX, destEndY)
        }

        // Draw the route line
        drawPath(
            path = path, color = lineColor, alpha = 0.7f, style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = pathEffect,
            )
        )

        val tipSize = 8.dp.toPx()

        drawArc(
            lineColor,
            startAngle = 90.0f,
            sweepAngle = 180.0f,
            useCenter = false,
            topLeft = Offset(destEndX - tipSize, destEndY - tipSize),
            size = Size(tipSize * 2, tipSize * 2),
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = pathEffect,
            )
        )
    }
}


@Preview(showBackground = false, showSystemUi = false)
@Composable
fun TodayPlanCardPreviewWork() {
    FreyzaEmployeeTheme {
        TodayPlanCard(
            planEntry = dummyTravelPlanEntryWork(),
            route = dummyRoute(),
            srcDestPair = dummyLocation() to dummyLocationAlt()
        )
    }
}

@Preview
@Composable
fun TodayPlanCardPreviewHoliday() {
    FreyzaEmployeeTheme {
        TodayPlanCard(
            planEntry = dummyTravelPlanEntryHoliday(), route = null, srcDestPair = null
        )
    }
}

@Preview
@Composable
fun TodayPlanCardPreviewLeave() {
    FreyzaEmployeeTheme {
        TodayPlanCard(
            planEntry = dummyTravelPlanEntryLeave(), route = null, srcDestPair = null
        )
    }
}


@Preview
@Composable
fun TodayPlanCardNullPreview() {
    FreyzaEmployeeTheme {
        TodayPlanCard(null, null, null)
    }
}
