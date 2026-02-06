package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.freyza.employee.R
import com.freyza.employee.core.Constants
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.toLocalDate
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaDefaultAppBar(modifier: Modifier = Modifier) {
  CenterAlignedTopAppBar(
    title = { Text("Freyza", maxLines = 1, overflow = TextOverflow.Ellipsis) },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ),
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaHomeAppBar(
  today: LocalDateTime?,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  var today = today
  if (today == null) {
    today = Clock.System.now().toLocalDateTime(
      TimeZone.of(
        Constants.TIMEZONE
      )
    )
  }
  TopAppBar(
    title = {
      Column(
        modifier = Modifier.padding(
          vertical = dimensionResource(R.dimen.default_spacing).times(4)
        )
      ) {
        Text(
          today.dayOfWeek.name.uppercase(),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
          color = MaterialTheme.colorScheme.secondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          DateFormatter.format(today.toLocalDate(), DateFormatter.FormattingType.HUMAN, true),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    actions = {
      IconButton(onClick = { /* do something */ }) {
        Icon(
          painter = painterResource(R.drawable.calendar_month_24px),
          contentDescription = null
        )
      }
    },
    scrollBehavior = scrollBehavior,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaTpAppBar(modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Text("Travel Plan", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }, colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaProfileAppBar(modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Text("Profile & Settings", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }, colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ), modifier = modifier
  )
}

@Composable
@Preview
private fun FreyzaDefaultAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaDefaultAppBar()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun FreyzaHomeAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaHomeAppBar(today = null, scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior())
  }
}

@Composable
@Preview
private fun FreyzaTpAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaTpAppBar()
  }
}


@Composable
@Preview
private fun FreyzaProfileAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaProfileAppBar()
  }
}
