package com.example.ui.screens

import com.example.i18n.tr

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.AnnouncerSettings
import com.example.data.HeadphoneMode
import com.example.i18n.AppLanguage
import com.example.ui.components.GroupDivider
import com.example.ui.components.ListRow
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.components.SwitchRow
import com.example.ui.theme.AccentAmber
import com.example.util.AnnouncementLanguage
import com.example.util.QuietHours

/** Callbacks for the settings added in v2.1 (language of the words, quiet hours, gestures, messages…). */
class MoreSettingsActions(
    val onAnnouncementLanguageChange: (AnnouncementLanguage) -> Unit,
    val onToggleSayNetwork: () -> Unit,
    val onToggleSpamWarning: () -> Unit,
    val onQuietHoursChange: (enabled: Boolean, start: Int, end: Int) -> Unit,
    val onHeadphoneModeChange: (HeadphoneMode) -> Unit,
    val onToggleStopWhenSilenced: () -> Unit,
    val onToggleFlipToStop: () -> Unit,
    val onToggleShakeToStop: () -> Unit,
    val onToggleMessages: () -> Unit,
    val onToggleReadMessageText: () -> Unit,
    val onToggleMissedSummary: () -> Unit,
    val contactRuleCount: Int,
    val onOpenContactRules: () -> Unit,
    val onExportBackup: () -> Unit,
    val onImportBackup: () -> Unit,
    val onAppLanguageChange: (AppLanguage) -> Unit
)

@Composable
fun AppLanguageRow(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        ListRow(
            icon = Icons.Default.Translate,
            title = tr("App language"),
            subtitle = tr(selected.label),
            onClick = { open = true },
            modifier = Modifier.testTag("app_language_row")
        ) {
            Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(tr(language.label)) },
                    onClick = {
                        onSelect(language)
                        open = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementLanguageRow(selected: AnnouncementLanguage, onSelect: (AnnouncementLanguage) -> Unit) {
    Column(modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg)) {
        Text(tr("Language of the words"), style = MaterialTheme.typography.titleSmall)
        Text(
            tr("\u201CIncoming call from\u201D, \u201Con WhatsApp\u201D, \u201Cunknown caller\u201D\u2026"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.sm))
        val languages = AnnouncementLanguage.entries
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            languages.forEachIndexed { index, language ->
                SegmentedButton(
                    selected = selected == language,
                    onClick = { onSelect(language) },
                    shape = SegmentedButtonDefaults.itemShape(index, languages.size),
                    icon = {},
                    modifier = Modifier.testTag("announcement_language_${language.name.lowercase()}")
                ) { Text(language.label, maxLines = 1) }
            }
        }
    }
}

@Composable
fun CallerDetailsRows(settings: AnnouncerSettings, actions: MoreSettingsActions) {
    SwitchRow(
        icon = Icons.Default.CellTower,
        title = tr("Say the network"),
        subtitle = tr("\u201CAn MTN number\u201D or \u201Can Airtel number\u201D before unsaved Rwandan numbers."),
        checked = settings.sayNetwork,
        onCheckedChange = { actions.onToggleSayNetwork() },
        modifier = Modifier.testTag("say_network_switch")
    )
    GroupDivider()
    SwitchRow(
        icon = Icons.Default.ReportProblem,
        title = tr("Warn about spam"),
        subtitle = tr("Says \u201CPossible spam\u201D first for numbers you marked as spam in History or that your phone blocked before."),
        checked = settings.spamWarning,
        onCheckedChange = { actions.onToggleSpamWarning() },
        modifier = Modifier.testTag("spam_warning_switch")
    )
}

@Composable
fun ContactRulesRow(actions: MoreSettingsActions) {
    ListRow(
        icon = Icons.Default.RecordVoiceOver,
        title = tr("Contacts & pronunciation"),
        subtitle = when (actions.contactRuleCount) {
            0 -> tr("Fix a name that sounds wrong, always or never announce someone")
            1 -> tr("1 contact with its own settings")
            else -> tr("%d contacts with their own settings", actions.contactRuleCount)
        },
        onClick = actions.onOpenContactRules,
        modifier = Modifier.testTag("contact_rules_row")
    ) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuietSection(settings: AnnouncerSettings, actions: MoreSettingsActions) {
    val context = LocalContext.current
    fun pickTime(initial: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onPicked(hour * 60 + minute) },
            initial / 60,
            initial % 60,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    Section(title = tr("Quiet times")) {
        SwitchRow(
            icon = Icons.Default.Bedtime,
            title = tr("Quiet hours"),
            subtitle = if (settings.quietHoursEnabled) {
                tr(
                    "Silent from %s to %s. Contacts set to \u201CAlways\u201D are still announced.",
                    QuietHours.format(settings.quietStartMinutes),
                    QuietHours.format(settings.quietEndMinutes)
                )
            } else tr("Stay silent at night; the phone still rings."),
            checked = settings.quietHoursEnabled,
            onCheckedChange = {
                actions.onQuietHoursChange(it, settings.quietStartMinutes, settings.quietEndMinutes)
            },
            modifier = Modifier.testTag("quiet_hours_switch")
        )
        if (settings.quietHoursEnabled) {
            GroupDivider()
            ListRow(
                icon = Icons.Default.Schedule,
                title = tr("From"),
                subtitle = QuietHours.format(settings.quietStartMinutes),
                onClick = {
                    pickTime(settings.quietStartMinutes) {
                        actions.onQuietHoursChange(true, it, settings.quietEndMinutes)
                    }
                }
            )
            GroupDivider()
            ListRow(
                icon = Icons.Default.Schedule,
                title = tr("Until"),
                subtitle = QuietHours.format(settings.quietEndMinutes),
                onClick = {
                    pickTime(settings.quietEndMinutes) {
                        actions.onQuietHoursChange(true, settings.quietStartMinutes, it)
                    }
                }
            )
        }
        GroupDivider()
        HeadphoneModeRow(settings.headphoneMode, actions.onHeadphoneModeChange)
    }
}

@Composable
private fun HeadphoneModeRow(selected: HeadphoneMode, onSelect: (HeadphoneMode) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        ListRow(
            icon = Icons.Default.Headphones,
            title = tr("With headphones or in the car"),
            subtitle = tr(selected.label),
            onClick = { open = true },
            modifier = Modifier.testTag("headphone_mode_row")
        ) {
            Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            HeadphoneMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(tr(mode.label))
                            Text(
                                mode.explanation(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onSelect(mode)
                        open = false
                    }
                )
            }
        }
    }
}

private fun HeadphoneMode.explanation() = when (this) {
    HeadphoneMode.SPEAKER_AND_HEADPHONES -> tr("Same as without headphones")
    HeadphoneMode.HEADPHONES_ONLY -> tr("Private, at a comfortable volume")
    HeadphoneMode.SILENT -> tr("Only the ringtone")
}

@Composable
fun StopSection(settings: AnnouncerSettings, actions: MoreSettingsActions) {
    Section(title = tr("Stop speaking")) {
        SwitchRow(
            icon = Icons.AutoMirrored.Filled.VolumeOff,
            title = tr("Volume or power button"),
            subtitle = tr("Silencing the ringtone also stops the announcement."),
            checked = settings.stopWhenSilenced,
            onCheckedChange = { actions.onToggleStopWhenSilenced() },
            modifier = Modifier.testTag("stop_when_silenced_switch")
        )
        GroupDivider()
        SwitchRow(
            icon = Icons.Default.ScreenRotation,
            title = tr("Turn the phone face down"),
            checked = settings.flipToStop,
            onCheckedChange = { actions.onToggleFlipToStop() },
            modifier = Modifier.testTag("flip_to_stop_switch")
        )
        GroupDivider()
        SwitchRow(
            icon = Icons.Default.Vibration,
            title = tr("Shake the phone"),
            checked = settings.shakeToStop,
            onCheckedChange = { actions.onToggleShakeToStop() },
            modifier = Modifier.testTag("shake_to_stop_switch")
        )
    }
}

@Composable
fun MessagesSection(
    settings: AnnouncerSettings,
    actions: MoreSettingsActions,
    notificationAccessMissing: Boolean,
    onAllowNotificationAccess: () -> Unit
) {
    Section(title = tr("Messages & missed calls")) {
        SwitchRow(
            icon = Icons.AutoMirrored.Filled.Message,
            title = tr("Announce messages"),
            subtitle = tr("\u201CMessage from Jean, on WhatsApp\u201D for WhatsApp, SMS, Telegram, Signal and more."),
            checked = settings.announceMessages,
            onCheckedChange = { actions.onToggleMessages() },
            modifier = Modifier.testTag("announce_messages_switch")
        )
        if (settings.announceMessages) {
            GroupDivider()
            SwitchRow(
                icon = Icons.AutoMirrored.Filled.ShortText,
                title = tr("Read the message too"),
                subtitle = tr("Anyone near the phone will hear it."),
                checked = settings.readMessageText,
                onCheckedChange = { actions.onToggleReadMessageText() },
                modifier = Modifier.testTag("read_message_text_switch")
            )
            if (notificationAccessMissing) {
                GroupDivider()
                ListRow(
                    icon = Icons.Default.NotificationsActive,
                    iconTint = AccentAmber,
                    title = tr("Notification access needed"),
                    subtitle = tr("Messages are found through their notifications."),
                    onClick = onAllowNotificationAccess
                ) {
                    FilledTonalButton(onClick = onAllowNotificationAccess) { Text(tr("Allow")) }
                }
            }
        }
        GroupDivider()
        SwitchRow(
            icon = Icons.Default.PhoneMissed,
            title = tr("Missed call summary"),
            subtitle = tr("When you unlock the phone: \u201CYou missed calls from Jean and Paul\u201D."),
            checked = settings.missedCallSummary,
            onCheckedChange = { actions.onToggleMissedSummary() },
            modifier = Modifier.testTag("missed_summary_switch")
        )
    }
}

@Composable
fun BackupSection(actions: MoreSettingsActions) {
    Section(title = tr("Backup")) {
        ListRow(
            icon = Icons.Default.FileUpload,
            title = tr("Save settings to a file"),
            subtitle = tr("Settings, contact pronunciations and spam numbers. Keep it for a new phone."),
            onClick = actions.onExportBackup,
            modifier = Modifier.testTag("export_backup_row")
        )
        GroupDivider()
        ListRow(
            icon = Icons.Default.FileDownload,
            title = tr("Restore from a file"),
            onClick = actions.onImportBackup,
            modifier = Modifier.testTag("import_backup_row")
        )
    }
}
