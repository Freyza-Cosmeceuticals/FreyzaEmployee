package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

data class FabActionItem(
  val text: String,
  @field:DrawableRes val icon: Int?,
  val onClick: () -> Unit,
)

@Composable
fun AddVisitFloatingActionButton(
  options: List<FabActionItem>,
  modifier: Modifier = Modifier,
  startExpanded: Boolean = false,
) {
  var isExpanded by rememberSaveable { mutableStateOf(startExpanded) }
  BackHandler(isExpanded) { isExpanded = false }

  // options + extended-fab
  Column(
    horizontalAlignment = Alignment.End, modifier = modifier
  ) {
    AnimatedVisibility(
      visible = isExpanded, enter = fadeIn() + expandIn(), exit = fadeOut() + shrinkOut()
    ) {
      // options list
      Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_spacing)),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.default_spacing).times(3))
      ) {
        options.forEach { opt ->
          FloatingActionButtonListItem(onClick = {
            opt.onClick()
            isExpanded = false
          }, icon = {
            opt.icon?.let {
              Icon(
                painter = painterResource(it),
                contentDescription = opt.text,
                modifier = Modifier.size(22.dp)
              )
            }
          }) {
            Text(
              text = opt.text,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    ExtendedFloatingActionButton(
      onClick = { isExpanded = !isExpanded },
      // Expand the pill when the menu is closed, collapse to a circle when open
      expanded = !isExpanded,
      shape = if (isExpanded) FloatingActionButtonDefaults.largeShape else FloatingActionButtonDefaults.extendedFabShape,
      icon = {
        AnimatedContent(
          targetState = isExpanded, transitionSpec = {
            (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
          }, label = "fab_icon_animation"
        ) { expanded ->
          if (expanded) {
            Icon(
              painter = painterResource(R.drawable.close_24px),
              contentDescription = "Close Menu"
            )
          } else {
            Icon(
              painter = painterResource(R.drawable.add_location_alt_24px),
              contentDescription = "Add visit"
            )
          }
        }
      },
      text = {
        Text(
          "Add Visit", style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium
        )
      })
  }
}

@Composable
private fun FloatingActionButtonListItem(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: (@Composable () -> Unit)? = null,
  text: @Composable () -> Unit,
) {
  FilledTonalButton(
    onClick = onClick,
    enabled = enabled,
    elevation = ButtonDefaults.filledTonalButtonElevation(
      defaultElevation = 4.dp, pressedElevation = 8.dp
    ),
    colors = ButtonDefaults.filledTonalButtonColors(
      containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ),
    shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
    modifier = modifier.height(56.dp)
  ) {
    Row(
      modifier = Modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {
      if (icon != null) {
        icon()
        Spacer(Modifier.size(dimensionResource(R.dimen.default_spacing).times(2)))
      }
      text()
    }
  }
}

@Preview
@Composable
private fun FloatingActionButtonPreviewClosed() {
  FreyzaEmployeeTheme {
    AddVisitFloatingActionButton(
      options = listOf(
        FabActionItem(
          text = "Item 1 With Long Name", icon = R.drawable.chevron_left_24px, onClick = {}),
        FabActionItem(text = "Item 2", icon = R.drawable.route_24px, onClick = {}),
        FabActionItem(text = "Item 3 With", icon = R.drawable.backspace_24px, onClick = {})
      ), startExpanded = false
    )
  }
}

@Preview
@Composable
private fun FloatingActionButtonPreview() {
  FreyzaEmployeeTheme {
    AddVisitFloatingActionButton(
      options = listOf(
        FabActionItem(
          text = "Item 1 With Long Name", icon = R.drawable.chevron_left_24px, onClick = {}),
        FabActionItem(text = "Item 2", icon = R.drawable.route_24px, onClick = {}),
        FabActionItem(text = "Item 3 With", icon = R.drawable.backspace_24px, onClick = {})
      ), startExpanded = true
    )
  }
}
