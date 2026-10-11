package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppSettings
import com.example.ui.CallAnnouncerApp
import com.example.ui.CallAnnouncerViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.isDark
import com.example.util.SetupState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appSettings = AppSettings.getInstance(this)
        setContent {
            val settings by appSettings.settingsFlow.collectAsState()
            val dark = settings.themeMode.isDark()

            // Status/navigation bar icons must follow the app theme, not only the system one
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            MyApplicationTheme(darkTheme = dark) {
                val viewModel: CallAnnouncerViewModel = viewModel()
                CallAnnouncerApp(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        SetupState.isAppInForeground = true
    }

    override fun onStop() {
        super.onStop()
        SetupState.isAppInForeground = false
    }
}
