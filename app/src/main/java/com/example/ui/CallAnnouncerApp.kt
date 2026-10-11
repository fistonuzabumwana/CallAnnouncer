package com.example.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.ui.components.CallAnnouncerTopBar
import com.example.ui.components.SimulatedCallBanner
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen

enum class ScreenTab {
    DASHBOARD,
    SETTINGS,
    HISTORY,
    PERMISSIONS
}

@Composable
fun CallAnnouncerApp(viewModel: CallAnnouncerViewModel) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(ScreenTab.DASHBOARD) }

    val settings by viewModel.settings.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val testPhoneNumber by viewModel.testPhoneNumber.collectAsState()
    val testContactName by viewModel.testContactName.collectAsState()
    val isSimulatingRinging by viewModel.isSimulatingRinging.collectAsState()
    val permissionStatus by viewModel.permissionsStatus.collectAsState()
    val voiceLanguages by viewModel.availableVoiceLanguages.collectAsState()

    val livePreview = viewModel.getLivePreview()

    // Permission launcher for dangerous permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions(context)
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions(context)
    }

    // Re-check when returning from system settings (e.g. battery optimization screen)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkPermissions(context)
    }

    fun requestPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD) {
        currentTab = ScreenTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CallAnnouncerTopBar(
                isEnabled = settings.isEnabled,
                isSpeaking = isSpeaking,
                onStopSpeech = { viewModel.stopTts() }
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DASHBOARD,
                    onClick = { currentTab = ScreenTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Tester") },
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { currentTab = ScreenTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Voice & Cadence") },
                    modifier = Modifier.testTag("tab_settings")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = {
                        if (callLogs.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${callLogs.size}") } }) {
                                Icon(Icons.Default.History, contentDescription = "History")
                            }
                        } else {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    },
                    label = { Text("Log") },
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.PERMISSIONS,
                    onClick = { currentTab = ScreenTab.PERMISSIONS },
                    icon = {
                        if (!permissionStatus.allGranted) {
                            BadgedBox(badge = { Badge { Text("!") } }) {
                                Icon(Icons.Default.Security, contentDescription = "Permissions")
                            }
                        } else {
                            Icon(Icons.Default.Security, contentDescription = "Permissions")
                        }
                    },
                    label = { Text("Permissions") },
                    modifier = Modifier.testTag("tab_permissions")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        settings = settings,
                        permissionStatus = permissionStatus,
                        testPhoneNumber = testPhoneNumber,
                        testContactName = testContactName,
                        livePreview = livePreview,
                        isSpeaking = isSpeaking,
                        onToggleMaster = { viewModel.toggleMasterEnabled() },
                        onPhoneNumberChange = { viewModel.setTestPhoneNumber(it) },
                        onContactNameChange = { viewModel.setTestContactName(it) },
                        onTestSpeak = { viewModel.testSpeakPreview() },
                        onSimulateCall = { viewModel.simulateIncomingCall() },
                        onStopSpeech = { viewModel.stopTts() },
                        onNavigateToPermissions = { currentTab = ScreenTab.PERMISSIONS },
                        onNavigateToSettings = { currentTab = ScreenTab.SETTINGS }
                    )
                }

                ScreenTab.SETTINGS -> {
                    SettingsScreen(
                        settings = settings,
                        availableLanguages = voiceLanguages,
                        onVoiceLanguageChange = { viewModel.updateVoiceLanguage(it) },
                        onPrefixChange = { viewModel.updatePrefix(it) },
                        onPairModeChange = { viewModel.updatePairMode(it) },
                        onRepeatCountChange = { viewModel.updateRepeatCount(it) },
                        onInitialDelayChange = { viewModel.updateInitialDelay(it) },
                        onSpeechRateChange = { viewModel.updateSpeechRate(it) },
                        onSpeechPitchChange = { viewModel.updateSpeechPitch(it) },
                        onToggleContacts = { viewModel.toggleAnnounceContacts() },
                        onToggleUnknowns = { viewModel.toggleAnnounceUnknowns() },
                        onToggleDuckRingtone = { viewModel.toggleDuckRingtone() },
                        onToggleSyncRingtone = { viewModel.toggleSyncRingtoneVolume() },
                        onTestSettings = { viewModel.testSpeakWithSettings() }
                    )
                }

                ScreenTab.HISTORY -> {
                    HistoryScreen(
                        callLogs = callLogs,
                        onReplayCall = { viewModel.replayCallLog(it) },
                        onDeleteCall = { viewModel.deleteCallLog(it) },
                        onClearAll = { viewModel.clearAllLogs() }
                    )
                }

                ScreenTab.PERMISSIONS -> {
                    PermissionsScreen(
                        permissionStatus = permissionStatus,
                        onRequestAllPermissions = { requestPermissions() },
                        onRequestCallScreeningRole = {
                            viewModel.createCallScreeningRoleIntent(context)?.let { roleLauncher.launch(it) }
                        }
                    )
                }
            }

            // Simulated Call Heads-Up Overlay Banner
            SimulatedCallBanner(
                visible = isSimulatingRinging,
                announcementResult = livePreview,
                isSpeaking = isSpeaking,
                onAnswer = { viewModel.answerSimulatedCall() },
                onDecline = { viewModel.stopTts() }
            )
        }
    }
}
