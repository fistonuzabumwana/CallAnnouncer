package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlin.math.roundToInt

class RingtoneVolumeDucker(private val context: Context) {

    private val tag = "RingtoneVolumeDucker"
    private val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var originalRingVolume: Int? = null
    private var isDucked = false
    private var focusRequest: AudioFocusRequest? = null

    /**
     * Calculates the current ringtone volume percentage (between 0.0f and 1.0f).
     */
    fun getRingVolumePercent(): Float {
        if (audioManager == null) return 1.0f
        return try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            if (max <= 0) return 1.0f
            val current = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            (current.toFloat() / max.toFloat()).coerceIn(0.1f, 1.0f)
        } catch (e: Exception) {
            Log.w(tag, "Failed to get ring volume percent", e)
            1.0f
        }
    }

    /**
     * Ducks the ringtone volume down so the voice announcement can be heard clearly.
     * Also synchronizes the target stream volume with the ringtone volume percentage.
     * Returns the sampled ring volume percentage (e.g., 0.9f for 90%).
     */
    @Synchronized
    fun duckRingtone(duckRatio: Float = 0.25f): Float {
        if (audioManager == null) return 1.0f

        val ringPercent = getRingVolumePercent()

        if (isDucked) {
            return ringPercent
        }

        try {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            originalRingVolume = current

            // Calculate ducked volume, ensuring it never drops to 0 (to avoid DND/silent triggers)
            val ducked = (current * duckRatio).roundToInt().coerceIn(1, current)

            if (current > 1 && ducked < current) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, ducked, 0)
                Log.d(tag, "Ducked ringtone volume from $current to $ducked (ratio $duckRatio)")
            }

            // Also request transient audio focus with ducking
            requestAudioFocus()

            isDucked = true
        } catch (e: Exception) {
            Log.w(tag, "Failed to duck ringtone volume", e)
        }

        return ringPercent
    }

    /**
     * Restores the ringtone volume to its original level before the announcement began.
     */
    @Synchronized
    fun restoreRingtone() {
        if (!isDucked || audioManager == null) {
            return
        }

        try {
            originalRingVolume?.let { original ->
                val current = audioManager.getStreamVolume(AudioManager.STREAM_RING)
                if (current != original) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, original, 0)
                    Log.d(tag, "Restored ringtone volume to original level: $original")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to restore ringtone volume", e)
        } finally {
            abandonAudioFocus()
            originalRingVolume = null
            isDucked = false
        }
    }

    private fun requestAudioFocus() {
        if (audioManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { /* no-op */ }
                    .build()

                focusRequest = request
                audioManager.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_RING,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to request audio focus", e)
        }
    }

    private fun abandonAudioFocus() {
        if (audioManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to abandon audio focus", e)
        }
    }
}
