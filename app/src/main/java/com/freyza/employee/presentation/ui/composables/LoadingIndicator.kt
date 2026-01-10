package com.freyza.employee.presentation.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Please hold tight while we load...",
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(message)
    }
}

@Preview(showSystemUi = false, showBackground = true)
@Composable
fun LoadingIndicatorPreview() {
    FreyzaEmployeeTheme {
        LoadingIndicator(message = "Preview Loading")
    }
}
