package com.freyza.employee.presentation.ui.unauthenticated.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.R
import com.freyza.employee.SnackbarType
import com.freyza.employee.core.UIState
import com.freyza.employee.presentation.ui.composables.FreyzaSnackbarHost
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.showTypedSnackbar
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
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

    ActualLoginScreen(
        uiState,
        onLoginWithEmailClicked,
        onLoginWithGoogleClicked,
        onNavigateToAuthenticatedRoute,
        modifier
    )
}

@Composable
private fun ActualLoginScreen(
    uiState: UIState<UserInfo>,
    onLoginWithEmailClicked: (email: String, password: String) -> Unit,
    onLoginWithGoogleClicked: () -> Unit,
    onNavigateToAuthenticatedRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val unknownErrorString = stringResource(R.string.error_unknown)

    Scaffold(snackbarHost = { FreyzaSnackbarHost(snackbarHostState) }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                                message = uiState.message?.trim()?.lines()?.first()
                                    ?: unknownErrorString,
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
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.login_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(
                Modifier
                    .padding(horizontal = 64.dp)
                    .padding(top = 36.dp, bottom = 32.dp),
                thickness = Dp.Hairline
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
                shape = RoundedCornerShape(32),
                value = email,
                onValueChange = { email = it },
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                label = {
                    Text(
                        stringResource(R.string.password_placeholder),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                singleLine = true,
                isError = uiState is UIState.Error,
                shape = RoundedCornerShape(32),
                visualTransformation = PasswordVisualTransformation(),
                value = password,
                onValueChange = { password = it },
            )
            Spacer(Modifier.height(32.dp))

            val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
            Button(
                onClick = {
                    localSoftwareKeyboardController?.hide()
                    onLoginWithEmailClicked(email, password)
                },
                shape = RoundedCornerShape(32),
                enabled = uiState !is UIState.Loading
            ) {
                if (uiState is UIState.Loading) {
                    CircularProgressIndicator(
                        gapSize = 4.dp,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Text(
                    if (uiState is UIState.Loading) "Logging in..." else stringResource(R.string.login_button),
                    style = MaterialTheme.typography.titleMedium
                )

            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.height(40.dp)) {
                if (error.isNullOrBlank().not()) {
                    Text(text = "Error: $error", color = Color.Red)
                }
            }

            //        Spacer(Modifier.height(16.dp))
            //        ElevatedButton(onClick = onLoginWithGoogleClicked) {
            //            Text("Login with Google")
            //        }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    FreyzaEmployeeTheme {
        ActualLoginScreen(
            uiState = UIState.Idle(),
            onLoginWithEmailClicked = { _, _ -> },
            onLoginWithGoogleClicked = {},
            onNavigateToAuthenticatedRoute = {})
    }
}
