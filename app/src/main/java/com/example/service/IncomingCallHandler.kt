package com.example.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CallLogEntity
import com.example.tts.AudioKeepAlive
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementBuilder
import com.example.util.AnnouncementVolumeController
import com.example.util.ContactLookupHelper
import com.example.util.DebugLog
import com.example.util.DeviceStorage
import com.example.util.IncomingVoipCall
import com.example.util.VoipCallParser
import com.example.util.PhoneCallNumberResolver
import com.example.util.PhoneNumberMatcher
import com.example.util.SetupState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * Single place that turns call-state events into announcements.
 *
 * Android delivers RINGING several times per call (call screening, the PHONE_STATE broadcast
 * once without and once with the number, and the service's telephony listener), so events
 * are de-duplicated per ringing session.
 */
object IncomingCallHandler {

    private const val TAG = "IncomingCallHandler"

    /** How long to wait for a RINGING event that carries the number before announcing without it. */
    private const val NUMBER_WAIT_MS = 700L

    private const val MAX_RING_SESSION_MS = 90_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isRinging = false
    private var ringingSince = 0L
    private var ringingNumber: String? = null
    private var announcementJob: Job? = null

    /**
     * Starts (or keeps) the announcement for the current ringing call.
     * Returns a job that completes once the announcement has finished speaking.
     */
    @Synchronized
    fun onRinging(
        context: Context,
        numberFromEvent: String?,
        onAnnouncing: ((callerIdentifier: String) -> Unit)? = null
    ): Job? {
        val appContext = context.applicationContext
        val number = numberFromEvent?.takeIf { it.isNotBlank() }

        // A screened call that never rang leaves no IDLE event, so stale sessions expire
        val sessionActive = isRinging &&
            SystemClock.elapsedRealtime() - ringingSince < MAX_RING_SESSION_MS
        if (sessionActive && (number == null || PhoneNumberMatcher.isSameNumber(number, ringingNumber))) {
            DebugLog.d(TAG, "Duplicate ringing event ignored")
            return announcementJob
        }

        isRinging = true
        ringingSince = SystemClock.elapsedRealtime()
        ringingNumber = number
        announcementJob?.cancel()
        AudioKeepAlive.start()
        DebugLog.d(TAG, "Ringing session started for $number")

        val job = scope.launch {
            try {
                if (number == null) delay(NUMBER_WAIT_MS)
                announce(appContext, number, onAnnouncing)
            } finally {
                stopKeepAliveIfCurrent(coroutineContext.job)
            }
        }
        announcementJob = job
        return job
    }

    @Synchronized
    private fun stopKeepAliveIfCurrent(job: Job) {
        if (announcementJob === job) {
            DebugLog.d(TAG, "Announcement finished")
            AudioKeepAlive.stop()
        }
    }

    @Synchronized
    fun onCallEnded(context: Context) {
        isRinging = false
        ringingNumber = null
        announcementJob?.cancel()
        announcementJob = null
        AudioKeepAlive.stop()
        DirectTtsAnnouncer.stop()
        // Also covers a fresh process where the announcer never ran but volumes were changed earlier
        AnnouncementVolumeController.getInstance(context).restore()
    }

    private var voipKey: String? = null
    private var voipSince = 0L
    private var voipJob: Job? = null

    /**
     * An internet calling app (WhatsApp, Telegram…) shows a ringing call notification.
     * Apps re-post that notification while it rings, so each notification is announced once.
     */
    @Synchronized
    fun onVoipRinging(context: Context, notificationKey: String, call: IncomingVoipCall, allowedByDoNotDisturb: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (voipKey == notificationKey && now - voipSince < MAX_RING_SESSION_MS) return
        if (isRinging && now - ringingSince < MAX_RING_SESSION_MS) {
            DebugLog.d(TAG, "Phone call already ringing; ignoring ${call.app.label} call")
            return
        }

        voipJob?.cancel()
        voipKey = notificationKey
        voipSince = now
        AudioKeepAlive.start()
        DebugLog.d(TAG, "${call.app.label} call ringing")

        val appContext = context.applicationContext
        val job = scope.launch {
            try {
                announceVoip(appContext, call, allowedByDoNotDisturb)
            } finally {
                stopKeepAliveIfCurrentVoip(coroutineContext.job)
            }
        }
        voipJob = job
    }

    /** The ringing notification was answered, declined, missed or replaced by an ongoing-call one. */
    @Synchronized
    fun onVoipEnded(context: Context, notificationKey: String) {
        if (voipKey != notificationKey) return
        DebugLog.d(TAG, "Internet call stopped ringing")
        voipKey = null
        voipJob?.cancel()
        voipJob = null
        if (isRinging) return
        AudioKeepAlive.stop()
        DirectTtsAnnouncer.stop()
        AnnouncementVolumeController.getInstance(context).restore()
    }

    @Synchronized
    private fun stopKeepAliveIfCurrentVoip(job: Job) {
        if (voipJob === job && !isRinging) AudioKeepAlive.stop()
    }

    private suspend fun announceVoip(context: Context, call: IncomingVoipCall, allowedByDoNotDisturb: Boolean) {
        val settings = AppSettings.getInstance(context).getSettings()
        if (!settings.isEnabled || !settings.hasAcceptedPrivacyPolicy) return
        val wanted = if (call.app.isWhatsApp) settings.announceWhatsApp else settings.announceOtherApps
        if (!wanted) {
            DebugLog.d(TAG, "${call.app.label} announcements disabled")
            return
        }

        // Unsaved callers often show as a number; look it up like a regular call
        val number = call.caller?.takeIf { VoipCallParser.looksLikePhoneNumber(it) }
        val contactName = if (number != null) ContactLookupHelper.lookupContactName(context, number) else call.caller
        val base = AnnouncementBuilder.buildAnnouncement(
            contactName = contactName,
            phoneNumber = number,
            prefix = settings.prefixText,
            pairMode = settings.pairMode
        )
        val announcement = AnnouncementBuilder.withCallingApp(base, call.app.label, call.isVideo)
        val isKnownContact = base.isKnownContact && !call.isUnsavedProfile

        if (isKnownContact && !settings.announceContacts) return
        if (!isKnownContact && !settings.announceUnknowns) return

        yield()
        val source = if (call.isVideo) "${call.app.label} video" else call.app.label
        saveToHistory(context, number, contactName, announcement.callerIdentifier,
            announcement.spokenText, isKnownContact, settings.repeatCount, source)

        val audioManager = context.getSystemService(AudioManager::class.java)
        if (audioManager?.mode == AudioManager.MODE_IN_CALL) {
            DebugLog.d(TAG, "On a phone call; not speaking over it")
            return
        }
        if (!allowedByDoNotDisturb) {
            DebugLog.d(TAG, "Do Not Disturb silences this ${call.app.label} call")
            return
        }

        yield()
        DebugLog.d(TAG, "Announcing: ${announcement.spokenText} (audio mode ${audioManager?.mode})")
        DirectTtsAnnouncer.speak(
            context = context,
            text = announcement.spokenText,
            repeatCount = settings.repeatCount,
            initialDelayMs = settings.initialDelayMs,
            repeatDelayMs = settings.repeatDelayMs,
            rate = settings.speechRate,
            pitch = settings.speechPitch,
            languageTag = settings.voiceLanguage,
            duckRingtone = settings.duckRingtone,
            syncRingtoneVolume = settings.syncRingtoneVolume,
            skipWhenSilenced = true,
            name = contactName
        ).join()
    }

    private suspend fun announce(
        context: Context,
        number: String?,
        onAnnouncing: ((String) -> Unit)?
    ) {
        val settings = AppSettings.getInstance(context).getSettings()
        if (!settings.isEnabled) {
            DebugLog.d(TAG, "Announcer disabled in settings; ignoring ring")
            return
        }
        if (!settings.hasAcceptedPrivacyPolicy) {
            DebugLog.d(TAG, "Privacy policy not accepted; ignoring ring")
            return
        }

        val incomingNumber = PhoneCallNumberResolver.resolveIncomingNumber(context, number)
        val contactName = ContactLookupHelper.lookupContactName(context, incomingNumber)
        val announcement = AnnouncementBuilder.buildAnnouncement(
            contactName = contactName,
            phoneNumber = incomingNumber,
            prefix = settings.prefixText,
            pairMode = settings.pairMode
        )

        if (announcement.isKnownContact && !settings.announceContacts) {
            DebugLog.d(TAG, "Known contact announcements disabled")
            return
        }
        if (!announcement.isKnownContact && !settings.announceUnknowns) {
            DebugLog.d(TAG, "Unknown caller announcements disabled")
            return
        }

        // The call may have been answered or rejected while looking up the contact
        yield()
        saveToHistory(context, incomingNumber, contactName, announcement.callerIdentifier,
            announcement.spokenText, announcement.isKnownContact, settings.repeatCount)

        if (isAnotherCallActive(context)) {
            DebugLog.d(TAG, "Already in a call; not speaking over it")
            return
        }
        if (isBlockedByDoNotDisturb(context, incomingNumber)) {
            DebugLog.d(TAG, "Do Not Disturb blocks this caller; staying silent")
            return
        }

        yield()
        DebugLog.d(TAG, "Announcing: ${announcement.spokenText}")
        onAnnouncing?.invoke(announcement.callerIdentifier)
        // Proves on this phone that calls reach the app while it is closed (shown in the setup checklist)
        if (!SetupState.isAppInForeground &&
            !AnnouncementVolumeController.getInstance(context).isRingerSilenced()
        ) {
            SetupState.recordBackgroundAnnouncement(context)
        }

        DirectTtsAnnouncer.speak(
            context = context,
            text = announcement.spokenText,
            repeatCount = settings.repeatCount,
            initialDelayMs = settings.initialDelayMs,
            repeatDelayMs = settings.repeatDelayMs,
            rate = settings.speechRate,
            pitch = settings.speechPitch,
            languageTag = settings.voiceLanguage,
            duckRingtone = settings.duckRingtone,
            syncRingtoneVolume = settings.syncRingtoneVolume,
            skipWhenSilenced = true,
            name = contactName
        ).join()
    }

    private suspend fun saveToHistory(
        context: Context,
        number: String?,
        contactName: String?,
        callerIdentifier: String,
        spokenText: String,
        isKnownContact: Boolean,
        repeatCount: Int,
        source: String? = null
    ) {
        // The history database lives in storage that is unreadable until the first unlock after boot
        if (!DeviceStorage.isUserUnlocked(context)) return
        try {
            AppDatabase.getInstance(context).callLogDao().insertAnnouncement(
                CallLogEntity(
                    rawPhoneNumber = number ?: "Unknown",
                    contactName = contactName,
                    formattedDisplay = callerIdentifier,
                    spokenAnnouncement = spokenText,
                    isKnownContact = isKnownContact,
                    timesAnnounced = repeatCount,
                    source = source
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist call log", e)
        }
    }

    /** A second call (call waiting) or a VoIP call is in progress. */
    private fun isAnotherCallActive(context: Context): Boolean {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return false
        return audioManager.mode == AudioManager.MODE_IN_CALL ||
            audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    private fun isBlockedByDoNotDisturb(context: Context, number: String?): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return false
        val filter = notificationManager.currentInterruptionFilter
        if (filter == NotificationManager.INTERRUPTION_FILTER_ALL ||
            filter == NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        ) {
            return false
        }
        // With "priority only", announce exactly the callers Do Not Disturb lets ring
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !number.isNullOrBlank()) {
            try {
                return !notificationManager.matchesCallFilter(Uri.fromParts("tel", number, null))
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot check Do Not Disturb call filter", e)
            }
        }
        return true
    }
}
