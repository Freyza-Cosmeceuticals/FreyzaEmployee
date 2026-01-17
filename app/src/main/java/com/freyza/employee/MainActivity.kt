package com.freyza.employee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.freyza.employee.presentation.ui.theme.FreyzaEmployeeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FreyzaEmployeeTheme {
        FreyzaEmployeeApp()
      }
    }
  }
}
