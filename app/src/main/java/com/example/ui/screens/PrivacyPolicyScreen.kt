package com.example.ui.screens

import com.example.i18n.tr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AnnouncerSettings
import com.example.ui.components.GroupDivider
import com.example.ui.components.IconTile
import com.example.ui.components.ListRow
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.theme.AccentGreen

private data class PolicyPoint(val icon: ImageVector, val title: String, val body: String)

private fun permissionsUsed() = listOf(
    PolicyPoint(
        Icons.Default.Phone,
        tr("Phone state and call log"),
        tr("Used only to notice an incoming call, read the caller's number so it can be spoken, " +
            "check whether your phone blocked that number before, and list missed calls if you turn on the summary.")
    ),
    PolicyPoint(
        Icons.Default.Contacts,
        tr("Contacts"),
        tr("Used only to look up the caller's name (and phonetic name, if saved) on your phone. " +
            "Your contact list is never copied or uploaded.")
    ),
    PolicyPoint(
        Icons.Default.NotificationsActive,
        tr("Notification access (optional)"),
        tr("Used to spot incoming-call notifications from WhatsApp, Telegram, Signal and similar apps. " +
            "Only if you turn on message announcements, new-message notifications of chat apps are read aloud. " +
            "Nothing from notifications is stored.")
    ),
    PolicyPoint(
        Icons.Default.Notifications,
        tr("Notifications"),
        tr("Shows that the announcer is running and lets you pause it.")
    ),
    PolicyPoint(
        Icons.Default.RecordVoiceOver,
        tr("Text-to-speech"),
        tr("Announcements are spoken by your phone's speech engine. Spoken audio is cached on the phone to start faster next time.")
    )
)

private fun storedData() = listOf(
    PolicyPoint(
        Icons.Default.History,
        tr("Announcement history"),
        tr("Date, number or name, the calling app (for app calls) and what was spoken, for each announced call. Kept only on this phone.")
    ),
    PolicyPoint(
        Icons.Default.Settings,
        tr("Your settings"),
        tr("Voice, wording, volume and theme preferences, how you want some contacts announced, and numbers you marked as spam.")
    )
)

private fun yourChoices() = listOf(
    PolicyPoint(
        Icons.Default.CloudOff,
        tr("Nothing leaves your phone"),
        tr("The app has no internet access. No analytics, no ads, no accounts, and no data is sold or shared.")
    ),
    PolicyPoint(
        Icons.Default.Backup,
        tr("Backups"),
        tr("A backup file is only created when you save one, in the place you choose. " +
            "If you turned on Android's own backup, it may include this app's settings and history, like any other app.")
    ),
    PolicyPoint(
        Icons.Default.DeleteOutline,
        tr("Delete anytime"),
        tr("Clear the history in the app, revoke permissions in Android settings, or uninstall to remove everything.")
    )
)

/**
 * Shows the privacy policy. With [onAccept] and [onDecline] it asks for consent;
 * with [onClose] it is a read-only view opened from Settings.
 */
@Composable
fun PrivacyPolicyScreen(
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    val asksForConsent = onAccept != null && onDecline != null

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onClose != null) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back"))
                    }
                    Text(tr("Privacy policy"), style = MaterialTheme.typography.titleLarge)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .then(if (onClose == null) Modifier.statusBarsPadding() else Modifier)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg)
                    .testTag("privacy_policy_screen"),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
            ) {
                Header()
                PointsSection(tr("What the app uses"), permissionsUsed())
                PointsSection(tr("What is stored"), storedData())
                PointsSection(tr("Your data, your control"), yourChoices())
                Text(
                    text = tr("Policy version %d. If this policy changes, you'll be asked to review it again.",
                        AnnouncerSettings.PRIVACY_POLICY_VERSION),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.xs)
                )
            }

            if (asksForConsent) {
                ConsentBar(onAccept = onAccept!!, onDecline = onDecline!!)
            } else {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.lg))
        IconTile(icon = Icons.Default.Shield, size = 72.dp, circle = true)
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = tr("Your privacy"),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = tr("Call Announcer speaks the name or number of whoever is calling. " +
                "Here's exactly what it needs to do that."),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PhonelinkLock,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.padding(end = Spacing.xs)
            )
            Text(
                text = tr("Everything stays on this phone"),
                style = MaterialTheme.typography.labelLarge,
                color = AccentGreen
            )
        }
    }
}

@Composable
private fun PointsSection(title: String, points: List<PolicyPoint>) {
    Section(title = title) {
        points.forEachIndexed { index, point ->
            if (index > 0) GroupDivider()
            ListRow(
                icon = point.icon,
                title = point.title,
                subtitle = point.body,
                subtitleMaxLines = Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun ConsentBar(onAccept: () -> Unit, onDecline: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(Spacing.lg)
            ) {
                Text(
                    text = tr("By tapping Accept, you agree to this privacy policy. The app can't announce calls without it."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = onDecline,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("privacy_decline_button")
                    ) {
                        Text(tr("Decline"))
                    }
                    Button(
                        onClick = onAccept,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("privacy_accept_button")
                    ) {
                        Text(tr("Accept"))
                    }
                }
            }
        }
    }
}
