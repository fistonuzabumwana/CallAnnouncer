package com.example.ui.screens

import com.example.i18n.tr

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.Group
import com.example.ui.components.GroupDivider
import com.example.ui.components.IconTile
import com.example.ui.components.ScreenColumn
import com.example.ui.components.Section
import com.example.ui.components.Spacing
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.util.Importance
import com.example.util.ReadinessCheck
import com.example.util.ReadinessReport

@Composable
fun SetupScreen(
    report: ReadinessReport,
    isGuiding: Boolean,
    onFixEverything: () -> Unit,
    onFix: (ReadinessCheck) -> Unit,
    onConfirm: (ReadinessCheck) -> Unit
) {
    ScreenColumn {
        SummaryCard(report, isGuiding, onFixEverything)

        Section(title = tr("Required")) {
            report.required.forEachIndexed { index, check ->
                if (index > 0) GroupDivider()
                CheckRow(check, onFix, onConfirm)
            }
        }

        Section(title = tr("Recommended")) {
            report.recommended.forEachIndexed { index, check ->
                if (index > 0) GroupDivider()
                CheckRow(check, onFix, onConfirm)
            }
        }

        Text(
            text = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.lg)
        )
    }
}

@Composable
private fun SummaryCard(report: ReadinessReport, isGuiding: Boolean, onFixEverything: () -> Unit) {
    val ready = report.isReady
    val total = report.required.size
    val done = total - report.failingRequired.size
    val accent = if (ready) AccentGreen else AccentAmber

    Group(
        modifier = Modifier.testTag("setup_summary_card"),
        color = accent.copy(alpha = 0.10f),
        contentPadding = PaddingValues(Spacing.xl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = if (ready) Icons.Default.VerifiedUser else Icons.Default.Warning,
                tint = accent,
                size = 52.dp,
                circle = true
            )
            Spacer(Modifier.width(Spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ready) tr("All set") else tr("%d of %d done", done, total),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (ready) tr("Calls will be announced, even when the app is closed.")
                    else tr("Finish the steps below so calls are announced when the app is closed."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        LinearProgressIndicator(
            progress = { if (total == 0) 1f else done.toFloat() / total },
            color = accent,
            trackColor = accent.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        if (report.fixable.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = onFixEverything,
                enabled = !isGuiding,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("fix_everything_button")
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(if (isGuiding) tr("Approve each screen…") else tr("Fix everything"))
            }
            Text(
                text = tr("Opens each setting in turn. Tap Allow, then come back; the next one opens automatically."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
    }
}

@Composable
private fun CheckRow(
    check: ReadinessCheck,
    onFix: (ReadinessCheck) -> Unit,
    onConfirm: (ReadinessCheck) -> Unit
) {
    val (icon, tint) = when {
        check.passed -> Icons.Default.CheckCircle to AccentGreen
        check.importance == Importance.REQUIRED -> Icons.Default.ErrorOutline to AccentRed
        check.fixLabel == null -> Icons.Default.Info to MaterialTheme.colorScheme.primary
        else -> Icons.Default.Warning to AccentAmber
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = 14.dp)
            .testTag("setup_check_${check.item.name.lowercase()}")
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconTile(icon = icon, tint = tint)
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = check.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = check.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!check.passed && check.fixLabel != null) {
            Row(modifier = Modifier.padding(start = 48.dp, top = Spacing.md)) {
                FilledTonalButton(onClick = { onFix(check) }, shape = MaterialTheme.shapes.small) {
                    Text(check.fixLabel)
                }
                if (check.needsConfirmation) {
                    Spacer(Modifier.width(Spacing.sm))
                    OutlinedButton(onClick = { onConfirm(check) }, shape = MaterialTheme.shapes.small) {
                        Text(tr("It's on"))
                    }
                }
            }
        }
    }
}
