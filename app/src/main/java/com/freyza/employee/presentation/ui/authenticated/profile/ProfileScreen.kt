package com.freyza.employee.presentation.ui.authenticated.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.core.UIState
import com.freyza.employee.core.util.Logger
import com.freyza.employee.domain.model.dummyUserEmployee
import com.freyza.employee.presentation.ui.authenticated.AuthenticatedRouteWrapper
import com.freyza.employee.presentation.ui.composables.FreyzaProfileAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.Skeleton
import com.freyza.employee.presentation.ui.state.MainUiState
import com.freyza.employee.presentation.ui.state.ProfileScreenUiState
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.ProfileViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoute(
  mainUiState: MainUiState,
  modifier: Modifier = Modifier,
  viewModel: ProfileViewModel = koinViewModel(),
  onNavigateToUnauthenticated: () -> Unit,
) {

  AuthenticatedRouteWrapper(
    mainUiState, onNavigateToUnauthenticated,
    loading = {
      Logger.e("ProfileScreenRoute", "Invalid User/Session on profile screen, waiting for 5seconds")
      ProfileScreenSkeleton(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)))
    },
    5_000,
  ) { mainUiState ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
      uiState, mainUiState, onNavigateToUnauthenticated, modifier
    )
  }
}

@Composable
fun ProfileScreen(
  uiS5tate: ProfileScreenUiState,
  mainUiState: MainUiState,
  onLogoutClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    topBar = { FreyzaProfileAppBar() },
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    )
  ) {
    if (mainUiState.user == null || mainUiState.today == null) {
      return@Scaffold
    }

    LazyColumn(
      contentPadding = PaddingValues(
        vertical = dimensionResource(R.dimen.default_spacing).times(8),
        horizontal = dimensionResource(R.dimen.default_spacing).times(4)
      ),
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(R.dimen.default_spacing).times(2), Alignment.Top
      ),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier
        .fillMaxSize()
        .padding(it)
    ) {
//      item {
//        Text(
//          "Profile and Settings",
//          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
//        )
//        Spacer(Modifier.height(16.dp))
//      }

      when (val data = uiS5tate.user) {
        is UIState.Ready -> {
          val user = data.data!!

          item {
            Skeleton(
              Modifier
                .width(96.dp)
                .height(96.dp)
                .clip(CircleShape)
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))
          }

          item {
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

          item {
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

          item {
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

          item {
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

          item {
            Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(4)))
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
        }

        is UIState.Error -> item { Text("Error loading profile data") }
        is UIState.Loading -> item { ProfileScreenSkeleton() }
        else -> {}
      }
    }
  }
}

@Composable
fun ProfileScreenSkeleton(modifier: Modifier = Modifier) {
  FreyzaEmployeeTheme {
    Skeleton(
      Modifier
        .width(32.dp)
        .height(32.dp)
    )
  }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ProfileScreenPreview() {
  FreyzaEmployeeTheme {
    ProfileScreen(
      uiS5tate = ProfileScreenUiState(user = UIState.Ready(dummyUserEmployee())),
      mainUiState = MainUiState(
        hasValidSession = true, user = dummyUserEmployee(), today = LocalDateTime(
          year = 2026, month = Month.JANUARY, day = 1, hour = 5, minute = 59, second = 59
        )
      ),
      onLogoutClicked = {},
    )
  }
}
