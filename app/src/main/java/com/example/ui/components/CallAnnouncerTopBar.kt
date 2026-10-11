package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallAnnouncerTopBar(
    title: String,
    isEnabled: Boolean,
    isReady: Boolean,
    isSpeaking: Boolean,
    onStopSpeech: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        },
        actions = {
            AnimatedVisibility(visible = isSpeaking, enter = fadeIn(), exit = fadeOut()) {
                FilledTonalButton(
                    onClick = onStopSpeech,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("stop_speaking_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Stop")
                }
            }
            // "Active" is only shown once the setup checklist confirms calls will really be announced
            val (statusText, statusColor) = when {
                !isEnabled -> "Paused" to MaterialTheme.colorScheme.onSurfaceVariant
                !isReady -> "Setup needed" to AccentAmber
                else -> "Active" to AccentGreen
            }
            StatusPill(
                text = statusText,
                color = statusColor,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .testTag("service_status_chip")
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}
