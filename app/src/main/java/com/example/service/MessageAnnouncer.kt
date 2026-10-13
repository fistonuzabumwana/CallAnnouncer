package com.example.service

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import com.example.data.AppSettings
import com.example.data.HeadphoneMode
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementBuilder
import com.example.util.AudioOutputs
import com.example.util.DebugLog
import com.example.util.IncomingMessage
import com.example.util.QuietHours

/** Speaks new chat messages ("Message from Jean, on WhatsApp") when the user turned it on. */
object MessageAnnouncer {

    private const val TAG = "MessageAnnouncer"
    private const val MIN_GAP_MS = 4_000L
    private const val SAME_MESSAGE_WINDOW_MS = 60_000L

    private var lastSpokenAt = 0L
    private var lastKey: String? = null

    @Synchronized
    fun onMessage(context: Context, message: IncomingMessage, allowedByDoNotDisturb: Boolean) {
        val settings = AppSettings.getInstance(context).getSettings()
        if (!settings.isEnabled || !settings.hasAcceptedPrivacyPolicy || !settings.announceMessages) return

        // Apps re-post a conversation's notification on every change; speak each message once
        val now = SystemClock.elapsedRealtime()
        val key = "${message.app}|${message.group}|${message.sender}|${message.text}"
        if (key == lastKey && now - lastSpokenAt < SAME_MESSAGE_WINDOW_MS) return
        if (now - lastSpokenAt < MIN_GAP_MS) {
            DebugLog.d(TAG, "Message arrived right after another; not speaking")
            return
        }

        val audioManager = context.getSystemService(AudioManager::class.java)
        if (audioManager != null && audioManager.mode != AudioManager.MODE_NORMAL) {
            DebugLog.d(TAG, "Phone is ringing or in a call; message not spoken")
            return
        }
        if (DirectTtsAnnouncer.isSpeaking.value) return
        if (!allowedByDoNotDisturb) return
        if (settings.quietHoursEnabled && QuietHours.isQuietNow(settings.quietStartMinutes, settings.quietEndMinutes)) return
        val personalOutput = AudioOutputs.personalOutput(context)
        if (personalOutput != null && settings.headphoneMode == HeadphoneMode.SILENT) return

        lastKey = key
        lastSpokenAt = now
        val announcement = AnnouncementBuilder.buildMessageAnnouncement(
            sender = message.sender,
            appLabel = message.app.label,
            language = settings.announcementLanguage,
            group = message.group,
            messageText = message.text.takeIf { settings.readMessageText }
        )
        DebugLog.d(TAG, "Announcing a ${message.app.label} message")
        DirectTtsAnnouncer.speak(
            context = context,
            text = announcement.spokenText,
            repeatCount = 1,
            initialDelayMs = 0L,
            rate = settings.speechRate,
            pitch = settings.speechPitch,
            languageTag = settings.voiceLanguage,
            duckRingtone = false,
            syncRingtoneVolume = false,
            skipWhenSilenced = true,
            announcement = announcement,
            headphonesOnly = personalOutput != null && settings.headphoneMode == HeadphoneMode.HEADPHONES_ONLY
        )
    }
}
