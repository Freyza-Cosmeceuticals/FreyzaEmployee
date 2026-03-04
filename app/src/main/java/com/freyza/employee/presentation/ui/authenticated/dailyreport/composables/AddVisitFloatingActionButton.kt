package com.freyza.employee.presentation.ui.authenticated.dailyreport.composables

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.R

data class FabActionItem(
  val text: String,
  @field:DrawableRes val icon: Int?,
  val onClick: () -> Unit,
)

@Composable
fun AddVisitFloatingActionButton(options: List<FabActionItem>, modifier: Modifier = Modifier) {
  var isExpanded by remember { mutableStateOf(false) }

  // options + efab
  Column(
    horizontalAlignment = Alignment.End, modifier = modifier
  ) {

    AnimatedVisibility(
      visible = isExpanded, enter = fadeIn() + expandIn(), exit = fadeOut() + shrinkOut()
    ) {
      // options list
      Column(
        verticalArrangement = Arrangement.spacedBy(
          dimensionResource(R.dimen.default_spacing).times(2)
        ),
        horizontalAlignment = Alignment.End,
        modifier = Modifier
          .padding(bottom = dimensionResource(R.dimen.default_spacing))
          .width(IntrinsicSize.Max)
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
                modifier = Modifier.size(24.dp)
              )
            }
          }) {
            Text(
              text = opt.text,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    ExtendedFloatingActionButton(
      onClick = { isExpanded = !isExpanded },
      // Expand the pill when the menu is closed, collapse to a circle when open
      expanded = !isExpanded, icon = {
        AnimatedContent(
          targetState = isExpanded, transitionSpec = {
            (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
          }, label = "fab_icon_animation"
        ) { expanded ->
          if (expanded) {
//            Icon(Icons.Default.Close, contentDescription = "Close menu")
            Text("X")
          } else {
            Text("+")
          }
        }
      }, text = {
        Text("Add Visit")
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
    onClick = onClick, enabled = enabled, elevation = ButtonDefaults.filledTonalButtonElevation(
      defaultElevation = 2.dp, pressedElevation = 4.dp
    ), modifier = modifier.height(56.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {
      if (icon != null) {
        icon()
        Spacer(Modifier.size(dimensionResource(R.dimen.default_spacing)))
      }
      text()
    }
  }
}

@Preview
@Composable
private fun FloatingActionButtonListItemPreview() {
  FloatingActionButtonListItem(onClick = {}, icon = null) {
    Text("Doctor")
  }
}

@Preview
@Composable
private fun AddVisitFloatingActionButtonPreview() {
  AddVisitFloatingActionButton(
    options = listOf(
      FabActionItem(
        text = "Item 1", icon = null, onClick = {}), FabActionItem(
        text = "Item 2", icon = null, onClick = {}), FabActionItem(
        text = "Item 3", icon = null, onClick = {})
    )
  )
}
