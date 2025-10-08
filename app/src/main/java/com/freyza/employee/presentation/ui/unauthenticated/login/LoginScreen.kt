package com.freyza.employee.presentation.ui.unauthenticated.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freyza.employee.common.Result
import com.freyza.employee.data.network.dto.UserDto
import com.freyza.employee.presentation.ui.viewmodels.LoginViewModel
import com.freyza.employee.util.Logger
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

    LoginScreen(
        uiState,
        onLoginWithEmailClicked,
        onLoginWithGoogleClicked,
        onNavigateToAuthenticatedRoute,
        modifier
    )
}

@Composable
fun LoginScreen(
    loginResult: Result<UserDto>?,
    onLoginWithEmailClicked: (email: String, password: String) -> Unit,
    onLoginWithGoogleClicked: () -> Unit,
    onNavigateToAuthenticatedRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))


        Button(onClick = { onLoginWithEmailClicked(email, password) }) {
            Text("Login")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onLoginWithGoogleClicked) {
            Text("Login with Google")
        }

        Spacer(Modifier.height(8.dp))

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
                CircularProgressIndicator()
            }

            else -> {}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        loginResult = null,
        onLoginWithEmailClicked = { _, _ -> },
        onLoginWithGoogleClicked = {},
        onNavigateToAuthenticatedRoute = {}
    )
}
