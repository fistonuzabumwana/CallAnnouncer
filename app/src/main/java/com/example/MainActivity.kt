package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CallAnnouncerApp
import com.example.ui.CallAnnouncerViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SetupState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
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
