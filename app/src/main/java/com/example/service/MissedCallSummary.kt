package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.AppSettings
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementBuilder
import com.example.util.ContactLookupHelper
import com.example.util.DebugLog
import com.example.util.DeviceStorage
import com.example.util.PhoneNumberFormatter
import com.example.util.QuietHours

/** When the phone is unlocked after missed calls, says who called ("You missed calls from Jean and Paul"). */
object MissedCallSummary {

    private const val TAG = "MissedCallSummary"
    private const val PREFS_NAME = "missed_call_summary"
    private const val KEY_LAST_SUMMARY = "last_summary_at"
    private const val MAX_AGE_MS = 12 * 60 * 60 * 1000L
    private const val MAX_NAMES = 4

    fun onUserPresent(context: Context) {
        val settings = AppSettings.getInstance(context).getSettings()
        if (!settings.isEnabled || !settings.hasAcceptedPrivacyPolicy || !settings.missedCallSummary) return
        if (settings.quietHoursEnabled && QuietHours.isQuietNow(settings.quietStartMinutes, settings.quietEndMinutes)) return
        // Unlocking to answer a ringing call must not replace that call's announcement
        val audioManager = context.getSystemService(AudioManager::class.java)
        if (audioManager != null && audioManager.mode != AudioManager.MODE_NORMAL) return
        if (DirectTtsAnnouncer.isSpeaking.value) return

        val prefs = DeviceStorage.prefs(context, PREFS_NAME)
        val now = System.currentTimeMillis()
        val since = maxOf(prefs.getLong(KEY_LAST_SUMMARY, 0L), now - MAX_AGE_MS)
        val callers = missedCallersSince(context, since)
        prefs.edit().putLong(KEY_LAST_SUMMARY, now).apply()
        if (callers.isEmpty()) return

        val summary = AnnouncementBuilder.buildMissedCallsSummary(callers.take(MAX_NAMES), settings.announcementLanguage)
        DebugLog.d(TAG, "Summarizing ${callers.size} missed callers")
        DirectTtsAnnouncer.speak(
            context = context,
            text = summary.spokenText,
            repeatCount = 1,
            initialDelayMs = 600L,
            rate = settings.speechRate,
            pitch = settings.speechPitch,
            languageTag = settings.voiceLanguage,
            duckRingtone = false,
            syncRingtoneVolume = false,
            skipWhenSilenced = true,
            announcement = summary
        )
    }

    /** Distinct callers of missed calls the user has not looked at yet, newest first. */
    private fun missedCallersSince(context: Context, since: Long): List<String> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) !=
            PackageManager.PERMISSION_GRANTED
        ) return emptyList()
        return try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME),
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = 1 AND ${CallLog.Calls.DATE} > ?",
                arrayOf(CallLog.Calls.MISSED_TYPE.toString(), since.toString()),
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        val number = cursor.getString(0)
                        val name = cursor.getString(1)?.takeIf { it.isNotBlank() }
                            ?: ContactLookupHelper.lookupContactName(context, number)
                            ?: number?.takeIf { it.isNotBlank() }?.let { PhoneNumberFormatter.formatInPairs(it) }
                            ?: continue
                        if (name !in this) add(name)
                    }
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Cannot read missed calls", e)
            emptyList()
        }
    }
}
