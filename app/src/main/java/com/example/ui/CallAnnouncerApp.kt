package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.components.CallAnnouncerTopBar
import com.example.ui.components.SimulatedCallBanner
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.util.SetupItem

enum class ScreenTab(
    val label: String,
    val title: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val tag: String
) {
    DASHBOARD("Home", "Call Announcer", Icons.Filled.Home, Icons.Outlined.Home, "tab_dashboard"),
    SETTINGS("Settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings"),
    HISTORY("History", "History", Icons.Filled.History, Icons.Outlined.History, "tab_history"),
    SETUP("Setup", "Setup", Icons.Filled.Shield, Icons.Outlined.Shield, "tab_setup")
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

    val activity = LocalContext.current as? Activity

    if (!settings.hasAcceptedPrivacyPolicy) {
        PrivacyPolicyScreen(
            onAccept = { viewModel.acceptPrivacyPolicy() },
            onDecline = { activity?.finishAndRemoveTask() }
        )
        return
    }

    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    if (showPrivacyPolicy) {
        BackHandler { showPrivacyPolicy = false }
        PrivacyPolicyScreen(onClose = { showPrivacyPolicy = false })
        return
    }

    val livePreview = viewModel.getLivePreview()
    val setupFixer = rememberSetupFixer(viewModel)
    val internetCallsCheck = readiness.checks.firstOrNull { it.item == SetupItem.INTERNET_CALLS }

    BackHandler(enabled = isSimulatingRinging) {
        viewModel.stopTts()
    }
    BackHandler(enabled = !isSimulatingRinging && currentTab != ScreenTab.DASHBOARD) {
        currentTab = ScreenTab.DASHBOARD
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CallAnnouncerTopBar(
                    title = currentTab.title,
                    isEnabled = settings.isEnabled,
                    isReady = readiness.isReady,
                    isSpeaking = isSpeaking,
                    onStopSpeech = { viewModel.stopTts() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    ScreenTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        val badge = if (tab == ScreenTab.SETUP) readiness.failingRequired.size else 0
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                BadgedBox(badge = { if (badge > 0) Badge { Text("$badge") } }) {
                                    Icon(
                                        if (selected) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.label
                                    )
                                }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.DASHBOARD -> DashboardScreen(
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

                    ScreenTab.SETTINGS -> SettingsScreen(
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
                        onTestSettings = { viewModel.testSpeakWithSettings() },
                        onThemeModeChange = { viewModel.updateThemeMode(it) },
                        onOpenPrivacyPolicy = { showPrivacyPolicy = true },
                        onToggleWhatsApp = { viewModel.toggleAnnounceWhatsApp() },
                        onToggleOtherApps = { viewModel.toggleAnnounceOtherApps() },
                        notificationAccessMissing = internetCallsCheck?.passed == false,
                        onAllowNotificationAccess = { internetCallsCheck?.let { setupFixer.fix(it) } }
                    )

                    ScreenTab.HISTORY -> HistoryScreen(
                        callLogs = callLogs,
                        onReplayCall = { viewModel.replayCallLog(it) },
                        onDeleteCall = { viewModel.deleteCallLog(it) },
                        onClearAll = { viewModel.clearAllLogs() }
                    )

                    ScreenTab.SETUP -> SetupScreen(
                        report = readiness,
                        isGuiding = setupFixer.isGuiding,
                        onFixEverything = { setupFixer.fixEverything() },
                        onFix = { setupFixer.fix(it) },
                        onConfirm = { viewModel.confirmAutoStart() }
                    )
                }
            }
        }

        SimulatedCallBanner(
            visible = isSimulatingRinging,
            announcementResult = livePreview,
            isSpeaking = isSpeaking,
            onAnswer = { viewModel.answerSimulatedCall() },
            onDecline = { viewModel.stopTts() }
        )
    }
}
