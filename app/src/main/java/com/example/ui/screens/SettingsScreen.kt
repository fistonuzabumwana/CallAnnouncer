package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FilledTonalButton
import com.example.ui.theme.AccentAmber
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AnnouncerSettings
import com.example.data.ThemeMode
import com.example.ui.components.GroupDivider
import com.example.ui.components.ListRow
import com.example.ui.components.ScreenColumn
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.components.SwitchRow
import com.example.util.PairSpokenMode
import java.util.Locale
import kotlin.math.roundToInt

private fun PairSpokenMode.shortTitle() = when (this) {
    PairSpokenMode.PAIRED_NUMBERS -> "Number pairs"
    PairSpokenMode.PAIRED_DIGIT_WORDS -> "Digit by digit"
    PairSpokenMode.PAIRED_SPACED_DIGITS -> "Spaced digits"
}

private fun PairSpokenMode.example() = when (this) {
    PairSpokenMode.PAIRED_NUMBERS -> "\u201Czero seven, eighty-five, forty-six\u201D"
    PairSpokenMode.PAIRED_DIGIT_WORDS -> "\u201Czero seven, eight five, four six\u201D"
    PairSpokenMode.PAIRED_SPACED_DIGITS -> "\u201Czero seven, 8 5, 4 6\u201D with short pauses"
}

private fun Locale.label(): String = getDisplayName(this).replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AnnouncerSettings,
    availableLanguages: List<Locale>,
    onVoiceLanguageChange: (String) -> Unit,
    onPrefixChange: (String) -> Unit,
    onPairModeChange: (PairSpokenMode) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    onInitialDelayChange: (Long) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onToggleContacts: () -> Unit,
    onToggleUnknowns: () -> Unit,
    onToggleDuckRingtone: () -> Unit,
    onToggleSyncRingtone: () -> Unit,
    onTestSettings: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onToggleWhatsApp: () -> Unit,
    onToggleOtherApps: () -> Unit,
    notificationAccessMissing: Boolean,
    onAllowNotificationAccess: () -> Unit
) {
    ScreenColumn {
        Section(title = "Voice") {
            LanguageRow(settings.voiceLanguage, availableLanguages, onVoiceLanguageChange)
            GroupDivider()
            SliderRow(
                title = "Speed",
                valueLabel = "${(settings.speechRate * 10).roundToInt() / 10f}×",
                value = settings.speechRate,
                range = 0.6f..1.5f,
                onChange = onSpeechRateChange,
                testTag = "speech_rate_slider"
            )
            SliderRow(
                title = "Pitch",
                valueLabel = "${(settings.speechPitch * 10).roundToInt() / 10f}×",
                value = settings.speechPitch,
                range = 0.6f..1.4f,
                onChange = onSpeechPitchChange,
                testTag = "speech_pitch_slider"
            )
            Button(
                onClick = onTestSettings,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg)
                    .height(48.dp)
                    .testTag("test_settings_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Play sample")
            }
        }

        Section(title = "What's said") {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                OutlinedTextField(
                    value = settings.prefixText,
                    onValueChange = onPrefixChange,
                    label = { Text("Before the caller's name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prefix_text_field")
                )
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    listOf("Incoming call from", "Call from", "Call alert:").forEach { phrase ->
                        SuggestionChip(onClick = { onPrefixChange(phrase) }, label = { Text(phrase) })
                    }
                }
            }
            GroupDivider()

            Text(
                text = "Reading unknown numbers",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = Spacing.lg, top = Spacing.lg, end = Spacing.lg)
            )
            PairSpokenMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPairModeChange(mode) }
                        .padding(horizontal = Spacing.sm, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = settings.pairMode == mode, onClick = { onPairModeChange(mode) })
                    Column(modifier = Modifier.padding(vertical = Spacing.sm)) {
                        Text(mode.shortTitle(), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            mode.example(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            GroupDivider()

            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("Say it", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(Spacing.sm))
                val counts = listOf(1, 2, 3, 5)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    counts.forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = settings.repeatCount == count,
                            onClick = { onRepeatCountChange(count) },
                            shape = SegmentedButtonDefaults.itemShape(index, counts.size)
                        ) {
                            Text(if (count == 1) "Once" else "${count}×")
                        }
                    }
                }
            }
            SliderRow(
                title = "Wait before speaking",
                subtitle = "The ringtone plays normally first, then lowers while the name is spoken.",
                valueLabel = "${settings.initialDelayMs / 1000f}s",
                value = settings.initialDelayMs.toFloat(),
                range = 0f..5000f,
                steps = 9,
                onChange = { onInitialDelayChange(it.toLong()) },
                testTag = "initial_delay_slider"
            )
            Spacer(Modifier.height(Spacing.sm))
        }

        Section(title = "Volume") {
            SwitchRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Louder than the ringtone",
                subtitle = "Speaks about 30% louder than your ringtone volume.",
                checked = settings.syncRingtoneVolume,
                onCheckedChange = { onToggleSyncRingtone() },
                modifier = Modifier.testTag("toggle_sync_ringtone_switch")
            )
            GroupDivider()
            SwitchRow(
                icon = Icons.AutoMirrored.Filled.VolumeDown,
                title = "Lower ringtone while speaking",
                subtitle = "Restored when the announcement ends or the call is answered.",
                checked = settings.duckRingtone,
                onCheckedChange = { onToggleDuckRingtone() },
                modifier = Modifier.testTag("toggle_duck_ringtone_switch")
            )
        }

        Section(title = "Who to announce") {
            SwitchRow(
                icon = Icons.Default.Person,
                title = "Saved contacts",
                subtitle = "Speaks the contact's name.",
                checked = settings.announceContacts,
                onCheckedChange = { onToggleContacts() }
            )
            GroupDivider()
            SwitchRow(
                icon = Icons.Default.PersonOff,
                title = "Unknown numbers",
                subtitle = "Reads the number in pairs.",
                checked = settings.announceUnknowns,
                onCheckedChange = { onToggleUnknowns() }
            )
        }

        Section(title = "Calls from apps") {
            SwitchRow(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "WhatsApp",
                subtitle = "Voice and video calls, including WhatsApp Business.",
                checked = settings.announceWhatsApp,
                onCheckedChange = { onToggleWhatsApp() },
                modifier = Modifier.testTag("toggle_whatsapp_switch")
            )
            GroupDivider()
            SwitchRow(
                icon = Icons.Default.Apps,
                title = "Other calling apps",
                subtitle = "Telegram, Signal, Messenger, Instagram, Teams, Google Meet, Viber, Skype, Zoom and more.",
                checked = settings.announceOtherApps,
                onCheckedChange = { onToggleOtherApps() },
                modifier = Modifier.testTag("toggle_other_apps_switch")
            )
            if (notificationAccessMissing && (settings.announceWhatsApp || settings.announceOtherApps)) {
                GroupDivider()
                ListRow(
                    icon = Icons.Default.NotificationsActive,
                    iconTint = AccentAmber,
                    title = "Notification access needed",
                    subtitle = "App calls are found through their call notifications. Only those are read.",
                    onClick = onAllowNotificationAccess,
                    modifier = Modifier.testTag("allow_notification_access_row")
                ) {
                    FilledTonalButton(onClick = onAllowNotificationAccess) { Text("Allow") }
                }
            }
        }

        Section(title = "Appearance") {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text("Theme", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(Spacing.sm))
                val modes = ThemeMode.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            icon = {},
                            modifier = Modifier.testTag("theme_${mode.name.lowercase()}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(mode.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(mode.label())
                            }
                        }
                    }
                }
            }
        }

        Section(title = "About") {
            ListRow(
                icon = Icons.Default.Shield,
                title = "Privacy policy",
                subtitle = "Everything stays on this phone",
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier.testTag("privacy_policy_row")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))
    }
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun ThemeMode.icon() = when (this) {
    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
    ThemeMode.LIGHT -> Icons.Default.LightMode
    ThemeMode.DARK -> Icons.Default.DarkMode
}

@Composable
private fun LanguageRow(
    selectedTag: String,
    languages: List<Locale>,
    onSelect: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val selected = languages.firstOrNull { it.toLanguageTag() == selectedTag }?.label() ?: "Phone default"

    Box {
        ListRow(
            icon = Icons.Default.Language,
            title = "Language",
            subtitle = selected,
            onClick = { open = true },
            modifier = Modifier.testTag("voice_language_button")
        ) {
            Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Phone default") },
                onClick = {
                    onSelect("")
                    open = false
                }
            )
            languages.forEach { locale ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(locale.label())
                            Text(
                                locale.getDisplayName(Locale.ENGLISH),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onSelect(locale.toLanguageTag())
                        open = false
                    }
                )
            }
            if (languages.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Download voices in Android Settings → Text-to-speech") },
                    onClick = { open = false },
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun SliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    testTag: String,
    subtitle: String? = null,
    steps: Int = 0
) {
    Column(modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.testTag(testTag)
        )
    }
}
