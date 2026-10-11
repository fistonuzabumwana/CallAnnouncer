package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.testTag
import com.example.ui.components.CallAnnouncerTopBar
import com.example.ui.components.SimulatedCallBanner
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen

enum class ScreenTab {
    DASHBOARD,
    SETTINGS,
    HISTORY,
    SETUP
}

@Composable
fun CallAnnouncerApp(viewModel: CallAnnouncerViewModel) {
    // Start on the checklist until the phone is fully set up
    var currentTab by rememberSaveable {
        mutableStateOf(if (viewModel.readiness.value.isReady) ScreenTab.DASHBOARD else ScreenTab.SETUP)
    }

    val settings by viewModel.settings.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val testPhoneNumber by viewModel.testPhoneNumber.collectAsState()
    val testContactName by viewModel.testContactName.collectAsState()
    val isSimulatingRinging by viewModel.isSimulatingRinging.collectAsState()
    val readiness by viewModel.readiness.collectAsState()
    val voiceLanguages by viewModel.availableVoiceLanguages.collectAsState()

    val livePreview = viewModel.getLivePreview()
    val setupFixer = rememberSetupFixer(viewModel)

    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD) {
        currentTab = ScreenTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CallAnnouncerTopBar(
                isEnabled = settings.isEnabled,
                isReady = readiness.isReady,
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
                    selected = currentTab == ScreenTab.SETUP,
                    onClick = { currentTab = ScreenTab.SETUP },
                    icon = {
                        val missing = readiness.failingRequired.size
                        if (missing > 0) {
                            BadgedBox(badge = { Badge { Text("$missing") } }) {
                                Icon(Icons.Default.Security, contentDescription = "Setup")
                            }
                        } else {
                            Icon(Icons.Default.Security, contentDescription = "Setup")
                        }
                    },
                    label = { Text("Setup") },
                    modifier = Modifier.testTag("tab_setup")
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
                        readiness = readiness,
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
                        onNavigateToSetup = { currentTab = ScreenTab.SETUP },
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

                ScreenTab.SETUP -> {
                    SetupScreen(
                        report = readiness,
                        isGuiding = setupFixer.isGuiding,
                        onFixEverything = { setupFixer.fixEverything() },
                        onFix = { setupFixer.fix(it) },
                        onConfirm = { viewModel.confirmAutoStart() }
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
