package com.freyza.employee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreyzaEmployeeTheme {
                Scaffold { paddingValues ->
                    Surface(modifier = Modifier.padding(paddingValues)) {
                        FreyzaEmployeeApp()
                    }
                }
            }
        }
    }
}
