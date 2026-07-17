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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.freyza.employee.R
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.ServerTime
import com.freyza.employee.core.util.toLocalDate
import com.freyza.employee.domain.model.VisitType
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaDefaultAppBar(modifier: Modifier = Modifier) {
  CenterAlignedTopAppBar(
    title = {
      Text(
        stringResource(R.string.app_bar_title_default),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }, colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaHomeAppBar(
  today: LocalDateTime,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    title = {
      Column(
        modifier = Modifier.padding(
          vertical = dimensionResource(R.dimen.default_spacing)
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
//    actions = {
//    IconButton(onClick = { /* do something */ }) {
//      Icon(
//        painter = painterResource(R.drawable.calendar_month_24px), contentDescription = null
//      )
//    }
//  },
    scrollBehavior = scrollBehavior, modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaTravelPlanAppBar(modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Text(
        stringResource(R.string.app_bar_title_travel_plan),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }, colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaDailyReportAppBar(modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Text(
        stringResource(R.string.app_bar_title_daily_reports),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }, colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaReportDetailAppBar(
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var enabled by remember { mutableStateOf(true) }

  TopAppBar(
    navigationIcon = {
    IconButton(
      onClick = {
        navigateUp()
      }, enabled = enabled
    ) {
      Icon(
        painterResource(R.drawable.arrow_back_24px),
        contentDescription = stringResource(R.string.content_description_navigate_back)
      )
    }
  }, title = {
    Text(
      "Daily Report", maxLines = 1, overflow = TextOverflow.Ellipsis
    )
  }, colors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    titleContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer),
    navigationIconContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer)
  ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaVisitDetailAppBar(
  visitType: VisitType?,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  onEditClick: (() -> Unit)? = null,
) {
  var enabled by remember { mutableStateOf(true) }

  TopAppBar(
    navigationIcon = {
    IconButton(
      onClick = {
        navigateUp()
      }, enabled = enabled
    ) {
      Icon(
        painterResource(R.drawable.arrow_back_24px),
        contentDescription = stringResource(R.string.content_description_navigate_back)
      )
    }
  }, title = {
    Text(
      if (visitType != null) "${visitType.titleCase()} Visit" else "Visit Details",
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }, actions = {
    if (onEditClick != null) {
      IconButton(onClick = onEditClick) {
        Icon(
          painter = painterResource(R.drawable.edit_24px),
          contentDescription = stringResource(R.string.content_description_edit_visit)
        )
      }
    }
  }, colors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    titleContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer),
    navigationIconContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer)
  ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaAddVisitAppBar(
  visitType: VisitType?,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  isEdit: Boolean = false,
) {
  var enabled by remember { mutableStateOf(true) }

  TopAppBar(
    navigationIcon = {
    IconButton(
      onClick = {
        navigateUp()
      }, enabled = enabled
    ) {
      Icon(
        painterResource(R.drawable.arrow_back_24px),
        contentDescription = stringResource(R.string.content_description_navigate_back)
      )
    }
  }, title = {
    if (visitType != null) {
      Text(
        stringResource(
          if (isEdit) R.string.app_bar_title_edit_specific_visit else R.string.app_bar_title_add_specific_visit,
          visitType.titleCase()
        ), maxLines = 1, overflow = TextOverflow.Ellipsis
      )
    } else {
      Text(
        stringResource(if (isEdit) R.string.app_bar_title_edit_visit else R.string.app_bar_title_add_visit),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }, colors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    titleContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer),
    navigationIconContentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer)
  ), modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaProfileAppBar(modifier: Modifier = Modifier) {
  TopAppBar(
    title = {
      Text(
        stringResource(R.string.app_bar_title_profile),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
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
    FreyzaHomeAppBar(
      today = ServerTime().nowLocalDateTime(),
      scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    )
  }
}

@Composable
@Preview
private fun FreyzaTravelPlanAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaTravelPlanAppBar()
  }
}

@Composable
@Preview
private fun FreyzaDailyReportAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaDailyReportAppBar()
  }
}

@Composable
@Preview
private fun FreyzaAddVisitAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaAddVisitAppBar(null, navigateUp = {})
  }
}

@Composable
@Preview
private fun FreyzaAddVisitWithTypeAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaAddVisitAppBar(VisitType.DOCTOR, navigateUp = {})
  }
}

@Composable
@Preview
private fun FreyzaProfileAppBarPreview() {
  FreyzaEmployeeTheme {
    FreyzaProfileAppBar()
  }
}
