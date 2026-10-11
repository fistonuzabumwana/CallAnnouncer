package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Controls volumes while a caller is announced:
 * - samples the ringtone volume,
 * - raises the announcement stream above the ringtone level,
 * - lowers the ringtone so the announcement is clearly heard,
 * - restores everything once the announcement or the call ends.
 *
 * Original volumes are persisted so they can be restored even if the process
 * was killed between the call starting and ending.
 */
class AnnouncementVolumeController private constructor(context: Context) {

    private val tag = "AnnouncementVolume"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val prefs: SharedPreferences = DeviceStorage.prefs(context, PREFS_NAME)
    private var focusRequest: AudioFocusRequest? = null

    /** True when the phone is in silent/vibrate mode or the ringtone volume is zero. */
    fun isRingerSilenced(): Boolean {
        val am = audioManager ?: return false
        return try {
            am.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
                am.getStreamVolume(AudioManager.STREAM_RING) == 0
        } catch (e: Exception) {
            false
        }
    }

    fun hasPendingRestore(): Boolean = prefs.contains(KEY_RING_VOLUME)

    @Synchronized
    fun begin(duckRingtone: Boolean, louderThanRingtone: Boolean) {
        val am = audioManager ?: return
        try {
            val ringMax = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            val announceMax = am.getStreamMaxVolume(ANNOUNCEMENT_STREAM)

            // Keep the first saved values if an announcement restarts within the same call
            val originalRing = prefs.getInt(KEY_RING_VOLUME, NOT_SET).takeIf { it != NOT_SET }
                ?: am.getStreamVolume(AudioManager.STREAM_RING)
            val originalAnnounce = prefs.getInt(KEY_ANNOUNCE_VOLUME, NOT_SET).takeIf { it != NOT_SET }
                ?: am.getStreamVolume(ANNOUNCEMENT_STREAM)
            prefs.edit()
                .putInt(KEY_RING_VOLUME, originalRing)
                .putInt(KEY_ANNOUNCE_VOLUME, originalAnnounce)
                .commit()

            if (louderThanRingtone && ringMax > 0 && announceMax > 0) {
                val target = announcementVolumeFor(originalRing, ringMax, originalAnnounce, announceMax)
                setVolume(am, ANNOUNCEMENT_STREAM, target)
                DebugLog.d(tag, "Ringtone at ${originalRing * 100 / ringMax}%; announcement volume $target/$announceMax")
            }

            if (duckRingtone && originalRing > 1) {
                val ducked = duckedRingVolume(originalRing)
                setVolume(am, AudioManager.STREAM_RING, ducked)
                DebugLog.d(tag, "Ringtone lowered from $originalRing to $ducked")
            }

            requestAudioFocus(am)
        } catch (e: Exception) {
            Log.w(tag, "Failed to prepare announcement volumes", e)
        }
    }

    @Synchronized
    fun restore() {
        val am = audioManager ?: return
        val ring = prefs.getInt(KEY_RING_VOLUME, NOT_SET)
        val announce = prefs.getInt(KEY_ANNOUNCE_VOLUME, NOT_SET)
        try {
            // Raising STREAM_RING above 0 would take the phone out of silent/vibrate mode
            if (ring != NOT_SET && am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                setVolume(am, AudioManager.STREAM_RING, ring)
            }
            if (announce != NOT_SET) {
                setVolume(am, ANNOUNCEMENT_STREAM, announce)
            }
            if (ring != NOT_SET || announce != NOT_SET) {
                DebugLog.d(tag, "Restored ringtone volume $ring and announcement volume $announce")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to restore volumes", e)
        } finally {
            prefs.edit().clear().commit()
            abandonAudioFocus(am)
        }
    }

    private fun setVolume(am: AudioManager, stream: Int, volume: Int) {
        try {
            if (am.getStreamVolume(stream) != volume) {
                am.setStreamVolume(stream, volume, 0)
            }
        } catch (e: SecurityException) {
            // Thrown while Do Not Disturb is active and the app has no DND access
            Log.w(tag, "Not allowed to change volume of stream $stream", e)
        }
    }

    private fun requestAudioFocus(am: AudioManager) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(ANNOUNCEMENT_ATTRIBUTES)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { }
                    .build()
                focusRequest = request
                am.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, ANNOUNCEMENT_STREAM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to request audio focus", e)
        }
    }

    private fun abandonAudioFocus(am: AudioManager) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to abandon audio focus", e)
        }
    }

    companion object {
        private const val PREFS_NAME = "announcement_volume_state"
        private const val KEY_RING_VOLUME = "original_ring_volume"
        private const val KEY_ANNOUNCE_VOLUME = "original_announce_volume"
        private const val NOT_SET = -1

        private const val DUCK_RATIO = 0.25f

        /** Announcement is played this fraction of the full range above the ringtone level. */
        private const val LOUDER_BOOST = 0.3f

        /** Alarm stream is independent from media, so a paused video keeps its own volume. */
        const val ANNOUNCEMENT_STREAM = AudioManager.STREAM_ALARM

        val ANNOUNCEMENT_ATTRIBUTES: AudioAttributes by lazy {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        }

        /** Announcement level: the ringtone level plus a boost, never below the user's own level. */
        fun announcementVolumeFor(ringVolume: Int, ringMax: Int, currentAnnounce: Int, announceMax: Int): Int {
            val ringFraction = ringVolume.toFloat() / ringMax
            return ceil((ringFraction + LOUDER_BOOST) * announceMax).toInt()
                .coerceIn(1, announceMax)
                .coerceAtLeast(currentAnnounce)
        }

        /** Ringtone level while speaking; never 0 so the phone does not switch to vibrate. */
        fun duckedRingVolume(ringVolume: Int): Int =
            (ringVolume * DUCK_RATIO).roundToInt().coerceIn(1, (ringVolume - 1).coerceAtLeast(1))

        @Volatile
        private var INSTANCE: AnnouncementVolumeController? = null

        fun getInstance(context: Context): AnnouncementVolumeController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnnouncementVolumeController(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
