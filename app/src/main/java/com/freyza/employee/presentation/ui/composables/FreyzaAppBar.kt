package com.freyza.employee.presentation.ui.composables

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreyzaAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = { Text("Freyza", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
@Preview
fun FreyzaAppBarPreview() {
    FreyzaEmployeeTheme {
        FreyzaAppBar()
    }
}
