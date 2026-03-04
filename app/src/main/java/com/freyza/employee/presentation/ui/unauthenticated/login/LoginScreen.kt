package com.freyza.employee.presentation.ui.unauthenticated.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.BuildConfig
import com.freyza.employee.R
import com.freyza.employee.core.SnackbarType
import com.freyza.employee.core.UIState
import com.freyza.employee.core.showTypedSnackbar
import com.freyza.employee.presentation.ui.composables.FreyzaDefaultAppBar
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.composables.VersionInfo
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoute(
  modifier: Modifier = Modifier,
  viewModel: LoginViewModel = koinViewModel(),
  onNavigateToRegistration: () -> Unit,
  onNavigateToAuthenticatedRoute: () -> Unit,
) {

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val onLoginWithEmailClicked = { email: String, password: String ->
    viewModel.loginWithEmail(email, password)
  }
  val onLoginWithGoogleClicked = { viewModel.loginWithGoogle() }

  LoginScreen(
    uiState,
    onLoginWithEmailClicked,
    onLoginWithGoogleClicked,
    onNavigateToAuthenticatedRoute,
    modifier
  )
}

@Composable
private fun LoginScreen(
  uiState: UIState<UserInfo>,
  onLoginWithEmailClicked: (email: String, password: String) -> Unit,
  onLoginWithGoogleClicked: () -> Unit,
  onNavigateToAuthenticatedRoute: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var email by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var error by rememberSaveable { mutableStateOf(uiState.message) }

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val unknownErrorString = stringResource(R.string.error_unknown)

  val focusManager = LocalFocusManager.current
  val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current

  var passwordVisible by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    topBar = { FreyzaDefaultAppBar() },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
      WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    ),
    snackbarHost = { FreyzaSnackbarHost(snackbarHostState) }) { it ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(it)
        .padding(dimensionResource(R.dimen.screen_padding)),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(
        modifier = modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {

        // handle screen transitions and error states
        LaunchedEffect(uiState) {
          when (uiState) {
            is UIState.Ready -> {
              onNavigateToAuthenticatedRoute()
            }

            is UIState.Error -> {
              error = uiState.message
              scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()

                snackbarHostState.showTypedSnackbar(
                  message = uiState.message?.trim()?.lines()?.first() ?: unknownErrorString,
                  type = SnackbarType.ERROR,
                  withDismissAction = true
                )
              }
            }

            is UIState.Loading -> {
              error = null
            }

            is UIState.Idle -> {
              error = null
            }
          }
        }

        Text(
          stringResource(R.string.app_title),
          style = MaterialTheme.typography.displayMedium.copy(
            fontSize = dimensionResource(R.dimen.title_font_size).value.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          ),
        )
        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))
        Text(
          stringResource(R.string.login_title), style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
          )
        )

        HorizontalDivider(
          Modifier
            .padding(horizontal = dimensionResource(R.dimen.screen_padding).times(4))
            .padding(
              top = dimensionResource(R.dimen.default_spacing).times(9),
              bottom = dimensionResource(R.dimen.default_spacing).times(8)
            ), thickness = Dp.Hairline
        )

        OutlinedTextField(
          label = {
            Text(
              stringResource(R.string.email_placeholder),
              style = MaterialTheme.typography.titleMedium
            )
          },
          singleLine = true,
          isError = uiState is UIState.Error,
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.mail_24px), contentDescription = null
            )
          },
          shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
          value = email,
          onValueChange = { email = it },
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next, keyboardType = KeyboardType.Email
          ),
          keyboardActions = KeyboardActions(
            onNext = {
              focusManager.moveFocus(FocusDirection.Down)
            }),
          modifier = Modifier
            .semantics { contentType = ContentType.EmailAddress }
            .widthIn(max = dimensionResource(R.dimen.login_controls_max_width))
            .fillMaxWidth())

        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(2)))

        OutlinedTextField(
          label = {
            Text(
              stringResource(R.string.password_placeholder),
              style = MaterialTheme.typography.titleMedium
            )
          },
          singleLine = true,
          isError = uiState is UIState.Error,
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.password_24px), contentDescription = null
            )
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                painter = if (passwordVisible) painterResource(R.drawable.visibility_24px) else painterResource(
                  R.drawable.visibility_off_24px
                ),
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
              )
            }
          },
          shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          value = password,
          onValueChange = { password = it },
          keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = KeyboardType.Password
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              onLoginWithEmailClicked(email, password)
            }),
          modifier = Modifier
            .semantics { contentType = ContentType.Password }
            .widthIn(max = dimensionResource(R.dimen.login_controls_max_width))
            .fillMaxWidth())

        Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(8)))

        Button(
          onClick = {
            localSoftwareKeyboardController?.hide()
            onLoginWithEmailClicked(email, password)
          },
          shape = RoundedCornerShape(integerResource(R.integer.rounding_radius)),
          enabled = uiState !is UIState.Loading,
          contentPadding = PaddingValues(dimensionResource(R.dimen.default_spacing).times(4)),
          modifier = Modifier
            .widthIn(max = dimensionResource(R.dimen.login_controls_max_width))
            .fillMaxWidth()
        ) {
          if (uiState is UIState.Loading) {
            CircularProgressIndicator(
              gapSize = 4.dp, strokeWidth = 2.dp, modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.default_spacing).times(3)))
          }

          Text(
            if (uiState is UIState.Loading) "Logging in..." else stringResource(R.string.login_button),
            style = MaterialTheme.typography.bodyLarge
          )
        }

        if (BuildConfig.DEBUG) {
          Spacer(Modifier.height(dimensionResource(R.dimen.default_spacing).times(8)))
          // debug error message box
          Box(
            Modifier
              .height(60.dp)
              .padding(horizontal = dimensionResource(R.dimen.screen_padding))
              .verticalScroll(rememberScrollState())
          ) {
            if (!error.isNullOrBlank()) {
              Text(
                text = "$error",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }

      VersionInfo(
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
  FreyzaEmployeeTheme {
    LoginScreen(
      uiState = UIState.Idle(),
      onLoginWithEmailClicked = { _, _ -> },
      onLoginWithGoogleClicked = {},
      onNavigateToAuthenticatedRoute = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenLoadingPreview() {
  FreyzaEmployeeTheme {
    LoginScreen(
      uiState = UIState.Loading(),
      onLoginWithEmailClicked = { _, _ -> },
      onLoginWithGoogleClicked = {},
      onNavigateToAuthenticatedRoute = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenErrorPreview() {
  FreyzaEmployeeTheme {
    LoginScreen(
      uiState = UIState.Error(
        "This is a long error message, with any kind of error may happen. Be ready for that. " +
                "This is wholesome in it's own that this error has occurred. " +
                "We are happy to announce that this is an error."
      ),
      onLoginWithEmailClicked = { _, _ -> },
      onLoginWithGoogleClicked = {},
      onNavigateToAuthenticatedRoute = {})
  }
}
