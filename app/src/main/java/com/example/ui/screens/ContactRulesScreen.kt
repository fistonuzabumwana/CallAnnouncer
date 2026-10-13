package com.example.ui.screens

import com.example.i18n.tr

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ContactPreference
import com.example.data.ContactRule
import com.example.ui.components.GroupDivider
import com.example.ui.components.IconTile
import com.example.ui.components.ListRow
import com.example.ui.components.ScreenColumn
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.theme.AccentAmber
import com.example.util.SpamChecker

/** A caller being edited: [number] identifies them, [displayName] is what is shown. */
data class ContactRuleDraft(
    val number: String,
    val displayName: String,
    val spokenName: String = "",
    val rule: ContactRule = ContactRule.NORMAL
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRulesScreen(
    rules: List<ContactPreference>,
    onBack: () -> Unit,
    onSave: (ContactRuleDraft) -> Unit,
    onRemove: (String) -> Unit,
    onPreview: (displayName: String, spokenName: String?) -> Unit
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ContactRuleDraft?>(null) }

    val pickContact = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode != Activity.RESULT_OK || uri == null) return@rememberLauncherForActivityResult
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val number = cursor.getString(0).orEmpty()
                val name = cursor.getString(1) ?: number
                val existing = rules.firstOrNull { it.key == SpamChecker.keyOf(number) }
                editing = ContactRuleDraft(number, name, existing?.spokenName.orEmpty(), existing?.rule ?: ContactRule.NORMAL)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(tr("Contacts & pronunciation")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    try {
                        pickContact.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
                    } catch (_: Exception) {
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(tr("Add contact")) },
                modifier = Modifier.testTag("add_contact_rule")
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (rules.isEmpty()) {
                EmptyRules()
            } else {
                ScreenColumn {
                    Section(title = tr("%d contacts", rules.size)) {
                        rules.forEachIndexed { index, rule ->
                            if (index > 0) GroupDivider()
                            ListRow(
                                icon = rule.rule.icon(),
                                iconTint = rule.rule.tint(),
                                title = rule.displayName,
                                subtitle = rule.summary(),
                                onClick = {
                                    editing = ContactRuleDraft(rule.key, rule.displayName, rule.spokenName.orEmpty(), rule.rule)
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(72.dp))
                }
            }
        }
    }

    editing?.let { draft ->
        ContactRuleDialog(
            draft = draft,
            canRemove = rules.any { it.key == SpamChecker.keyOf(draft.number) },
            onDismiss = { editing = null },
            onSave = {
                onSave(it)
                editing = null
            },
            onRemove = {
                SpamChecker.keyOf(draft.number)?.let(onRemove)
                editing = null
            },
            onPreview = onPreview
        )
    }
}

@Composable
fun ContactRuleDialog(
    draft: ContactRuleDraft,
    canRemove: Boolean,
    onDismiss: () -> Unit,
    onSave: (ContactRuleDraft) -> Unit,
    onRemove: () -> Unit,
    onPreview: (displayName: String, spokenName: String?) -> Unit
) {
    var spoken by remember(draft) { mutableStateOf(draft.spokenName) }
    var rule by remember(draft) { mutableStateOf(draft.rule) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(draft.displayName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    tr("Sounds wrong? Write the name the way it sounds, for example \u201CChoo-sah\u201D for Cyusa."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = spoken,
                        onValueChange = { spoken = it },
                        label = { Text(tr("Say it as")) },
                        placeholder = { Text(draft.displayName) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spoken_name_field")
                    )
                    IconButton(onClick = { onPreview(draft.displayName, spoken.ifBlank { null }) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = tr("Listen"), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(tr("Announce"), style = MaterialTheme.typography.titleSmall)
                val rules = ContactRule.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    rules.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = rule == option,
                            onClick = { rule = option },
                            shape = SegmentedButtonDefaults.itemShape(index, rules.size),
                            icon = {}
                        ) { Text(option.shortLabel(), maxLines = 1) }
                    }
                }
                Text(
                    rule.explanation(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft.copy(spokenName = spoken.trim(), rule = rule)) }) { Text(tr("Save")) }
        },
        dismissButton = {
            Row {
                if (canRemove) TextButton(onClick = onRemove) { Text(tr("Reset")) }
                TextButton(onClick = onDismiss) { Text(tr("Cancel")) }
            }
        }
    )
}

@Composable
private fun EmptyRules() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconTile(Icons.Default.RecordVoiceOver, size = 72.dp, circle = true)
            Spacer(Modifier.height(Spacing.lg))
            Text(tr("Make every name sound right"), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = tr("Fix how a name is pronounced, always announce important people, or never announce someone. " +
                    "You can also do this from History."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun ContactPreference.summary(): String {
    val said = spokenName?.let { tr("Said as \u201C%s\u201D", it) }
    val ruleText = when (rule) {
        ContactRule.NORMAL -> null
        ContactRule.VIP -> tr("Always announced, even in quiet hours")
        ContactRule.NEVER -> tr("Never announced")
    }
    return listOfNotNull(said, ruleText).joinToString(" · ").ifEmpty { tr("Normal") }
}

private fun ContactRule.shortLabel() = when (this) {
    ContactRule.NORMAL -> tr("Normal")
    ContactRule.VIP -> tr("Always")
    ContactRule.NEVER -> tr("Never")
}

private fun ContactRule.explanation() = when (this) {
    ContactRule.NORMAL -> tr("Follows your settings.")
    ContactRule.VIP -> tr("Announced even during quiet hours or when contacts are switched off.")
    ContactRule.NEVER -> tr("Their calls ring normally but are never announced.")
}

private fun ContactRule.icon() = when (this) {
    ContactRule.NORMAL -> Icons.Default.RecordVoiceOver
    ContactRule.VIP -> Icons.Default.Star
    ContactRule.NEVER -> Icons.Default.NotificationsOff
}

@Composable
private fun ContactRule.tint() = when (this) {
    ContactRule.NORMAL -> MaterialTheme.colorScheme.primary
    ContactRule.VIP -> AccentAmber
    ContactRule.NEVER -> MaterialTheme.colorScheme.onSurfaceVariant
}
