package com.example.tts

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.util.DebugLog
import kotlin.math.sqrt

data class StopOptions(
    val stopWhenSilenced: Boolean,
    val flipToStop: Boolean,
    val shakeToStop: Boolean
) {
    val any: Boolean get() = stopWhenSilenced || flipToStop || shakeToStop
}

/**
 * Watches for the user wanting silence while an announcement plays:
 * the ringtone being silenced (volume or power button), the phone turned face down, or a shake.
 */
class StopGestures(
    context: Context,
    options: StopOptions,
    private val onStop: () -> Unit
) {
    private val stopWhenSilenced = options.stopWhenSilenced
    private val flipToStop = options.flipToStop
    private val shakeToStop = options.shakeToStop
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var triggered = false
    private var ringtoneSeen = false
    private var faceDownSince = 0L
    private var startedFaceDown: Boolean? = null
    private val shakeTimes = ArrayDeque<Long>()

    private val playbackCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        object : AudioManager.AudioPlaybackCallback() {
            override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>) {
                val ringing = configs.any { it.audioAttributes.usage == AudioAttributes.USAGE_NOTIFICATION_RINGTONE }
                if (ringing) ringtoneSeen = true
                else if (ringtoneSeen) fire("ringtone silenced")
            }
        }
    } else null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val (x, y, z) = event.values
            val now = SystemClock.elapsedRealtime()
            if (flipToStop) checkFlip(z, now)
            if (shakeToStop) checkShake(x, y, z, now)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        if (stopWhenSilenced && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
            val callback = playbackCallback as AudioManager.AudioPlaybackCallback
            ringtoneSeen = audioManager.activePlaybackConfigurations
                .any { it.audioAttributes.usage == AudioAttributes.USAGE_NOTIFICATION_RINGTONE }
            audioManager.registerAudioPlaybackCallback(callback, mainHandler)
        }
        if (flipToStop || shakeToStop) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI, mainHandler)
            }
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                audioManager?.unregisterAudioPlaybackCallback(playbackCallback as AudioManager.AudioPlaybackCallback)
            } catch (_: Exception) {
            }
        }
        sensorManager?.unregisterListener(sensorListener)
    }

    private fun checkFlip(z: Float, now: Long) {
        val faceDown = z < FACE_DOWN_Z
        // Lying face down before the call started is not a gesture
        if (startedFaceDown == null) startedFaceDown = faceDown
        if (startedFaceDown == true) {
            if (!faceDown) startedFaceDown = false
            return
        }
        if (!faceDown) {
            faceDownSince = 0L
        } else if (faceDownSince == 0L) {
            faceDownSince = now
        } else if (now - faceDownSince > FACE_DOWN_HOLD_MS) {
            fire("phone turned face down")
        }
    }

    private fun checkShake(x: Float, y: Float, z: Float, now: Long) {
        val force = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        if (force < SHAKE_FORCE) return
        if (shakeTimes.lastOrNull()?.let { now - it < SHAKE_DEBOUNCE_MS } == true) return
        shakeTimes.addLast(now)
        while (shakeTimes.isNotEmpty() && now - shakeTimes.first() > SHAKE_WINDOW_MS) shakeTimes.removeFirst()
        if (shakeTimes.size >= SHAKES_NEEDED) fire("phone shaken")
    }

    private fun fire(reason: String) {
        if (triggered) return
        triggered = true
        DebugLog.d("StopGestures", "Stopping announcement: $reason")
        onStop()
    }

    private companion object {
        const val FACE_DOWN_Z = -8f
        const val FACE_DOWN_HOLD_MS = 400L
        const val SHAKE_FORCE = 11f
        const val SHAKE_DEBOUNCE_MS = 150L
        const val SHAKE_WINDOW_MS = 1200L
        const val SHAKES_NEEDED = 3
    }
}
