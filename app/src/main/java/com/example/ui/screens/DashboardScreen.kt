package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AnnouncerSettings
import com.example.ui.components.Group
import com.example.ui.components.GroupDivider
import com.example.ui.components.IconTile
import com.example.ui.components.ListRow
import com.example.ui.components.ScreenColumn
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.util.AnnouncementResult
import com.example.util.ReadinessReport

private data class Sample(val label: String, val name: String, val number: String)

private val samples = listOf(
    Sample("Unknown number", "", "0785461234"),
    Sample("Saved contact", "Alice", "0788123456"),
    Sample("MTN contact", "Yves MTN", "0788000111"),
    Sample("International", "", "+447854699011")
)

@Composable
fun DashboardScreen(
    settings: AnnouncerSettings,
    readiness: ReadinessReport,
    testPhoneNumber: String,
    testContactName: String,
    livePreview: AnnouncementResult,
    isSpeaking: Boolean,
    onToggleMaster: () -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onContactNameChange: (String) -> Unit,
    onTestSpeak: () -> Unit,
    onSimulateCall: () -> Unit,
    onStopSpeech: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ScreenColumn {
        StatusHero(
            enabled = settings.isEnabled,
            readiness = readiness,
            onToggle = onToggleMaster,
            onFinishSetup = onNavigateToSetup
        )

        Section(title = "Try it") {
            Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    samples.forEach { sample ->
                        FilterChip(
                            selected = testContactName == sample.name && testPhoneNumber == sample.number,
                            onClick = {
                                onContactNameChange(sample.name)
                                onPhoneNumberChange(sample.number)
                            },
                            label = { Text(sample.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = testPhoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone number") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_phone_number_input")
                )
                OutlinedTextField(
                    value = testContactName,
                    onValueChange = onContactNameChange,
                    label = { Text("Contact name (optional)") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_contact_name_input")
                )

                PreviewBubble(livePreview)

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = if (isSpeaking) onStopSpeech else onTestSpeak,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("test_voice_button")
                    ) {
                        Icon(
                            if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSpeaking) "Stop" else "Play voice")
                    }
                    Button(
                        onClick = onSimulateCall,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("simulate_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simulate call")
                    }
                }
            }
        }

        Section(title = "How it works") {
            ListRow(
                icon = Icons.Default.Person,
                title = "Saved contacts",
                subtitle = "Says the name you saved, e.g. \"Incoming call from Alice\"."
            )
            GroupDivider()
            ListRow(
                icon = Icons.Default.Dialpad,
                title = "Unknown numbers",
                subtitle = "Reads the number two digits at a time: 07 85 46 12 34."
            )
            GroupDivider()
            ListRow(
                icon = Icons.AutoMirrored.Filled.VolumeDown,
                title = "Ringtone lowered while speaking",
                subtitle = "Restored as soon as you answer, decline, or the call ends."
            )
            GroupDivider()
            ListRow(
                icon = Icons.Default.Tune,
                title = "Customize voice and wording",
                onClick = onNavigateToSettings
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

@Composable
private fun StatusHero(
    enabled: Boolean,
    readiness: ReadinessReport,
    onToggle: () -> Unit,
    onFinishSetup: () -> Unit
) {
    val missing = readiness.failingRequired.size
    val (accent, title, body) = when {
        !enabled -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Announcer is paused",
            "Turn it on to hear who is calling."
        )
        missing > 0 -> Triple(
            AccentAmber,
            "Almost ready",
            "$missing setup ${if (missing == 1) "step" else "steps"} left before calls are announced while the app is closed."
        )
        else -> Triple(
            AccentGreen,
            "You're covered",
            "Every incoming call will be announced, even when the app is closed."
        )
    }

    Group(
        modifier = Modifier.testTag("master_service_card"),
        color = if (enabled) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentPadding = PaddingValues(Spacing.xl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon = Icons.Default.Campaign, tint = accent, size = 52.dp, circle = true)
            Spacer(Modifier.width(Spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("master_toggle_switch")
            )
        }
        if (enabled && missing > 0) {
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = onFinishSetup,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("permission_alert_card")
            ) {
                Text("Finish setup")
            }
        }
    }
}

@Composable
private fun PreviewBubble(preview: AnnouncementResult) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_interpretation_card")
    ) {
        Row(modifier = Modifier.padding(Spacing.lg), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(
                    text = "YOU'LL HEAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "\u201C${preview.spokenText}\u201D",
                    style = MaterialTheme.typography.titleMedium,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        preview.hasMtnCarrier -> "Saved contact · \"MTN\" is spelled out as M-T-N"
                        preview.isKnownContact -> "Saved contact · name is spoken"
                        else -> "Unknown number · read in pairs"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}