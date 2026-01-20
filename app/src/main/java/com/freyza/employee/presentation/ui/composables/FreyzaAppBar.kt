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
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

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
fun FreyzaHomeAppBar(scrollBehavior: TopAppBarScrollBehavior, modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Column(
        modifier = Modifier.padding(
          vertical = dimensionResource(R.dimen.default_spacing).times(4)
        )
      ) {
        Text(
          "Tuesday".uppercase(),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
          color = MaterialTheme.colorScheme.secondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          "January 20",
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
fun FreyzaDefaultAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaDefaultAppBar()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun FreyzaHomeAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaHomeAppBar(TopAppBarDefaults.enterAlwaysScrollBehavior())
  }
}

@Composable
@Preview
fun FreyzaProfileAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaProfileAppBar()
  }
}
