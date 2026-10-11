package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(report, isGuiding, onFixEverything)

        Text(
            text = "Required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        report.required.forEach { CheckCard(it, onFix, onConfirm) }

        Text(
            text = "Recommended",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        report.recommended.forEach { CheckCard(it, onFix, onConfirm) }

        Text(
            text = "Phone: ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SummaryCard(report: ReadinessReport, isGuiding: Boolean, onFixEverything: () -> Unit) {
    val ready = report.isReady
    val total = report.required.size
    val done = total - report.failingRequired.size
    val accent = if (ready) AccentGreen else AccentAmber

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("setup_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (ready) Icons.Default.VerifiedUser else Icons.Default.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (ready) "Ready: calls will be announced" else "Setup needed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (ready) {
                            "Everything this phone needs is in place, even when the app is closed."
                        } else {
                            "$done of $total required steps done. Until all are done, calls may not be announced when the app is closed."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 1f else done.toFloat() / total },
                color = accent,
                modifier = Modifier.fillMaxWidth()
            )

            if (report.fixable.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onFixEverything,
                    enabled = !isGuiding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fix_everything_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isGuiding) "Fixing… approve each screen" else "Fix everything", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Opens each Android setting in turn. Tap Allow (or switch it on), then come back; the next one opens automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckCard(
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("setup_check_${check.item.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = check.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = check.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!check.passed && check.fixLabel != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onFix(check) }, shape = RoundedCornerShape(12.dp)) {
                        Text(check.fixLabel)
                    }
                    if (check.needsConfirmation) {
                        OutlinedButton(onClick = { onConfirm(check) }, shape = RoundedCornerShape(12.dp)) {
                            Text("I turned it on")
                        }
                    }
                }
            } else if (check.passed && check.needsConfirmation) {
                TextButton(onClick = { onFix(check) }) {
                    Text("Check again", color = Color.Unspecified)
                }
            }
        }
    }
}
