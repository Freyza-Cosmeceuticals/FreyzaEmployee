package com.freyza.employee.presentation.ui.unauthenticated.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.common.Result
import com.freyza.employee.presentation.ui.composables.LoadingScreen
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.util.Logger
import io.github.jan.supabase.auth.user.UserInfo
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
    loginResult: Result<UserInfo>?,
    onLoginWithEmailClicked: (email: String, password: String) -> Unit,
    onLoginWithGoogleClicked: () -> Unit,
    onNavigateToAuthenticatedRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Freyza Cosmo", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Employee Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(64.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))


        Button(onClick = { onLoginWithEmailClicked(email, password) }) {
            Text("Login")
        }
        Spacer(Modifier.height(16.dp))
        ElevatedButton(onClick = onLoginWithGoogleClicked) {
            Text("Login with Google")
        }

        when (val result = loginResult) {
            is Result.Success -> {
                LaunchedEffect(Unit) { onNavigateToAuthenticatedRoute() }
            }

            is Result.Error -> {
                val error = result.message
                Logger.d("APP", "Error logging in: $error")
                Text(text = "Error: $error", color = Color.Red)
            }

            is Result.Loading -> {
                LoadingScreen(message = "Logging you in...")
            }

            else -> {}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    FreyzaEmployeeTheme {
        ActualLoginScreen(
            loginResult = null,
            onLoginWithEmailClicked = { _, _ -> },
            onLoginWithGoogleClicked = {},
            onNavigateToAuthenticatedRoute = {}
        )
    }
}
