package com.freyza.employee.presentation.ui.authenticated.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.util.DateFormatter
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.User
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.composables.FreyzaProfileAppBar
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.composables.VersionInfo
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import com.freyza.employee.presentation.ui.state.dummyProfileScreenUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: ProfileViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  if (currentUser == null) {
    Logger.e("ProfileScreenRoute", "Invalid User/Session on profile screen")
    ProfileScreenSkeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
  } else {
    ProfileScreen(
      uiState = uiState,
      user = currentUser!!,
      onLogoutClicked = onNavigateToUnauthenticated,
      modifier = modifier
    )
  }
}

@Composable
fun ProfileScreen(
  uiState: ProfileScreenUiState,
  user: User,
  onLogoutClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = { FreyzaProfileAppBar() }
  ) { paddingValues ->
    LazyColumn(
      contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .imePadding()
    ) {
      item("dp") {
        Skeleton(
          Modifier
            .width(96.dp)
            .height(96.dp)
            .clip(CircleShape)
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))
      }

      item("name_details") {
        Text(
          user.name,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.padding(bottom = dimensionResource(R.dimen.default_spacing))
        )

        Text(
          "Medical Representative".uppercase(),
          style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.default_spacing).div(2),
            alignment = Alignment.CenterHorizontally
          ), verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            user.tier?.fullForm ?: "No Tier",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
          )
          Text("•")
          Text(
            user.hqId?.substring(0, 5) ?: "No HQ",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
          )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(6)))
      }

      item("acc_settings") {
        Text(
          "Account Settings".uppercase(), style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Start
          ), modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = dimensionResource(R.dimen.default_spacing).times(2),
              vertical = dimensionResource(R.dimen.default_spacing)
            )
        )
        ListItem(
          headlineContent = { Text(user.email) }, trailingContent = {
            Icon(
              painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
            )
          }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier
            .clip(
              RoundedCornerShape(
                topStart = dimensionResource(R.dimen.default_spacing).times(4),
                topEnd = dimensionResource(R.dimen.default_spacing).times(4)
              )
            )
            .clickable {})

        HorizontalDivider()

        ListItem(headlineContent = { Text(user.phone) }, trailingContent = {
          Icon(
            painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
          )
        }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier.clickable {})

        HorizontalDivider()

        ListItem(
          headlineContent = { Text("Change Password") }, trailingContent = {
            Icon(
              painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
            )
          }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier
            .clip(
              RoundedCornerShape(
                bottomStart = dimensionResource(R.dimen.default_spacing).times(4),
                bottomEnd = dimensionResource(R.dimen.default_spacing).times(4)
              )
            )
            .clickable {})

        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(4)))
      }

      item("app_preferences") {
        Text(
          "App Preferences".uppercase(), style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Start
          ), modifier = Modifier
            .fillMaxWidth()
            .padding(
              horizontal = dimensionResource(R.dimen.default_spacing).times(2),
              vertical = dimensionResource(R.dimen.default_spacing)
            )
        )

        ListItem(
          headlineContent = { Text("Language") }, trailingContent = {
            Icon(
              painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
            )
          }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier
            .clip(
              RoundedCornerShape(
                topStart = dimensionResource(R.dimen.default_spacing).times(4),
                topEnd = dimensionResource(R.dimen.default_spacing).times(4)
              )
            )
            .clickable {})

        HorizontalDivider()

        ListItem(headlineContent = { Text("Dark Mode") }, trailingContent = {
          Icon(
            painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
          )
        }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier.clickable {})

        HorizontalDivider()

        ListItem(
          headlineContent = { Text("Permissions") }, trailingContent = {
            Icon(
              painterResource(R.drawable.line_end_arrow_notch_24px), contentDescription = null
            )
          }, tonalElevation = 8.dp, shadowElevation = 0.dp, modifier = Modifier
            .clip(
              RoundedCornerShape(
                bottomStart = dimensionResource(R.dimen.default_spacing).times(4),
                bottomEnd = dimensionResource(R.dimen.default_spacing).times(4)
              )
            )
            .clickable {})
      }

      item("logout_btn") {
        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(8)))
        Button(
          onClick = onLogoutClicked,
          shape = RoundedCornerShape(dimensionResource(R.dimen.default_spacing).times(4)),
          colors = ButtonDefaults.filledTonalButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.default_spacing).times(4))
        ) { Text("Logout") }
      }

      item("footer") {
        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(4)))

        Text(
          "Server time ${DateFormatter.format(uiState.today)}",
          style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.tertiary
          )
        )

        VersionInfo()
        Text(
          "Freyza Employee App", style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.tertiary
          )
        )
        Text(
          "Developed by Harsh Narayan Jha", style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.tertiary
          )
        )
      }

      if (BuildConfig.DEBUG) {
        item("debug_info") {
          val horizontalScrollState = rememberScrollState()

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .horizontalScroll(horizontalScrollState)
          ) {
            Text(
              text = "Supabase URL: ${BuildConfig.SUPABASE_URL}",
              style = MaterialTheme.typography.labelMedium,
              softWrap = false
            )
            Text(
              text = "Supabase Publishable Key: ${BuildConfig.SUPABASE_PUBLISHABLE_KEY.substring(0..22)}...",
              style = MaterialTheme.typography.labelMedium,
              softWrap = false
            )
            Text(
              text = "Application ID: ${BuildConfig.APPLICATION_ID}",
              style = MaterialTheme.typography.labelMedium,
              softWrap = false
            )
          }
        }

        item("userinfo") {
          DebugUser(user)
        }
      }
    }
  }
}

@Composable
private fun DebugUser(user: User) {
  Card {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(user.id, fontFamily = FontFamily.Monospace)
      Text(user.name)
      Text(user.email)
      Text(user.phone)

      Text("Role: ${user.role.titleCase()}")
      Text("Status: ${user.status.titleCase()}")

      Text("Tier: ${user.tier?.toString()}")
      Text("hqId: ${user.hqId.toString()}")

      Text("joiningDate: ${DateFormatter.format(user.joiningDate)}")
      user.resignDate?.let {
        Text(DateFormatter.format(it))
      }

      Text("createdAt: ${DateFormatter.format(user.createdAt)}")
      user.updatedAt?.let {
        Text(DateFormatter.format(it))
      }

      user.userInfo?.lastSignInAt?.let {
        Text("lastSignIn: ${DateFormatter.format(it)}")
      }

      Text("userInfo.userMetadata: ${user.userInfo?.userMetadata}")
      Text("userInfo.appMetadata: ${user.userInfo?.appMetadata}")
    }
  }
}

@Composable
fun ProfileScreenSkeleton(modifier: Modifier = Modifier) {
  FreyzaEmployeeTheme {
    Skeleton(
      modifier
        .width(32.dp)
        .height(32.dp)
    )
  }
}

@Preview(showSystemUi = false, showBackground = false)
@Composable
private fun ProfileScreenPreview() {
  FreyzaEmployeeTheme {
    ProfileScreen(
      uiState = dummyProfileScreenUiState(),
      user = dummyUserEmployee(),
      onLogoutClicked = {},
    )
  }
}
