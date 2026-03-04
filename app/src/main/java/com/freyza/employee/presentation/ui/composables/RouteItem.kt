package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.domain.model.RouteWithLocation
import com.freyza.employee.domain.model.dummyRouteWithLocation
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun RouteItem(
  route: RouteWithLocation?,
  modifier: Modifier = Modifier,
  includeLeadingIcon: Boolean = true,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically, modifier = modifier
  ) {
    if (includeLeadingIcon) {
      Icon(
        painter = painterResource(R.drawable.route_24px),
        contentDescription = "Route",
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.width(dimensionResource(R.dimen.default_spacing)))
    }

    if (route != null) {
      Text(
        text = route.srcLoc.name,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Icon(
        painter = painterResource(R.drawable.arrow_right_24px),
        contentDescription = "to",
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = route.destLoc.name,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    } else {
      Text(
        text = "No Route",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun RouteItemPreview() {
  FreyzaEmployeeTheme {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      RouteItem(dummyRouteWithLocation())
      RouteItem(dummyRouteWithLocation(), includeLeadingIcon = false)
      RouteItem(null)
    }
  }
}
