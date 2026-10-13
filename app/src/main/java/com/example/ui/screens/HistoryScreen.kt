package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CallLogEntity
import com.example.ui.components.IconTile
import com.example.ui.components.SectionHeader
import com.example.ui.components.Spacing
import com.example.util.SpamChecker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    callLogs: List<CallLogEntity>,
    onReplayCall: (CallLogEntity) -> Unit,
    onDeleteCall: (CallLogEntity) -> Unit,
    onClearAll: () -> Unit,
    spamNumbers: Set<String> = emptySet(),
    onEditContact: (CallLogEntity) -> Unit = {},
    onSetSpam: (CallLogEntity, Boolean) -> Unit = { _, _ -> }
) {
    if (callLogs.isEmpty()) {
        EmptyHistory()
        return
    }

    var confirmClear by remember { mutableStateOf(false) }
    val groups = remember(callLogs) { callLogs.groupBy { dayLabel(it.timestamp) }.toList() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groups.forEachIndexed { groupIndex, (label, logs) ->
            item(key = "header_$label") {
                SectionHeader(
                    title = label,
                    modifier = Modifier.padding(top = if (groupIndex == 0) 0.dp else Spacing.xl),
                    action = if (groupIndex == 0) {
                        {
                            TextButton(
                                onClick = { confirmClear = true },
                                modifier = Modifier.testTag("clear_history_button")
                            ) { Text("Clear all") }
                        }
                    } else null
                )
            }
            itemsIndexed(logs, key = { _, log -> log.id }) { index, log ->
                CallLogRow(
                    log = log,
                    isFirst = index == 0,
                    isLast = index == logs.lastIndex,
                    onReplay = { onReplayCall(log) },
                    onDelete = { onDeleteCall(log) },
                    isSpam = SpamChecker.keyOf(log.rawPhoneNumber) in spamNumbers,
                    onEditContact = { onEditContact(log) },
                    onSetSpam = { onSetSpam(log, it) }
                )
            }
        }
        item { Spacer(Modifier.height(Spacing.lg)) }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = { Text("All ${callLogs.size} announced calls will be removed from this list.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CallLogRow(
    log: CallLogEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onReplay: () -> Unit,
    onDelete: () -> Unit,
    isSpam: Boolean,
    onEditContact: () -> Unit,
    onSetSpam: (Boolean) -> Unit
) {
    val hasNumber = SpamChecker.keyOf(log.rawPhoneNumber) != null
    var menuOpen by remember { mutableStateOf(false) }
    val corner = 24.dp
    val shape = RoundedCornerShape(
        topStart = if (isFirst) corner else 0.dp,
        topEnd = if (isFirst) corner else 0.dp,
        bottomStart = if (isLast) corner else 0.dp,
        bottomEnd = if (isLast) corner else 0.dp
    )
    val context = LocalContext.current
    val time = remember(log.timestamp) { DateFormat.getTimeFormat(context).format(Date(log.timestamp)) }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("call_log_item_${log.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.xs, top = Spacing.md, bottom = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallerAvatar(log)
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = log.contactName ?: log.formattedDisplay,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "\u201C${log.spokenAnnouncement}\u201D",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSpam) {
                        Text(
                            text = "Marked as spam",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (log.source != null) {
                        Text(
                            text = if (log.rawPhoneNumber == "Unknown") "${log.source} call" else "${log.source} call · ${log.rawPhoneNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (log.contactName != null) {
                        Text(
                            text = log.rawPhoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = onReplay) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Replay announcement",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("call_log_menu_${log.id}")) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (hasNumber) {
                            DropdownMenuItem(
                                text = { Text("Pronunciation & rules") },
                                leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    onEditContact()
                                }
                            )
                        }
                        if (hasNumber && !log.isKnownContact) {
                            DropdownMenuItem(
                                text = { Text(if (isSpam) "Not spam" else "Mark as spam") },
                                leadingIcon = { Icon(Icons.Default.ReportProblem, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    onSetSpam(!isSpam)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun CallerAvatar(log: CallLogEntity) {
    val name = log.contactName
    if (name.isNullOrBlank()) {
        IconTile(Icons.Default.Dialpad, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 40.dp, circle = true)
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconTile(Icons.Outlined.History, size = 72.dp, circle = true)
            Spacer(Modifier.height(Spacing.lg))
            Text("No calls yet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "Announced calls appear here, so you can replay what was said.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun dayLabel(timestamp: Long): String {
    val day = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    fun sameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        sameDay(day, today) -> "Today"
        sameDay(day, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
