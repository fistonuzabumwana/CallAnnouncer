package com.example.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.util.RingtoneVolumeDucker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

object DirectTtsAnnouncer : TextToSpeech.OnInitListener {

    private const val TAG = "DirectTtsAnnouncer"

    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var ringtoneDucker: RingtoneVolumeDucker? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var announcementJob: Job? = null
    private var pendingUtteranceId: String? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            val powerManager = appContext?.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CallAnnouncer::DirectTtsWakeLock")
            ringtoneDucker = RingtoneVolumeDucker(appContext!!)
        }

        if (tts == null && appContext != null) {
            try {
                tts = TextToSpeech(appContext, this)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to instantiate TextToSpeech", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                engine.setAudioAttributes(audioAttributes)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.w(TAG, "TTS utterance error: $errorCode on $utteranceId")
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }
                })

                _isReady.value = true
                Log.d(TAG, "TTS initialized and ready")
            }
        } else {
            Log.e(TAG, "TTS initialization failed code: $status")
            _isReady.value = false
        }
    }

    fun speak(
        context: Context,
        text: String,
        repeatCount: Int = 1,
        initialDelayMs: Long = 2000L,
        repeatDelayMs: Long = 1500L,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        duckRingtone: Boolean = true,
        syncRingtoneVolume: Boolean = true,
        onFinish: (() -> Unit)? = null
    ) {
        initialize(context)
        stop()

        announcementJob = scope.launch {
            acquireWakeLock()

            // Wait briefly if initializing
            var waitCount = 0
            while (!_isReady.value && waitCount < 20) {
                delay(100)
                waitCount++
            }

            if (!_isReady.value || tts == null) {
                Log.w(TAG, "TTS not ready to speak announcement")
                releaseWakeLock()
                return@launch
            }

            // 1. Initial delay before speaking: ringtone plays at normal volume
            if (initialDelayMs > 0) {
                delay(initialDelayMs)
            }

            // 2. Sample current ringtone volume and duck ringtone so speech is clearly heard
            val ringPercent = if (duckRingtone) {
                ringtoneDucker?.duckRingtone(duckRatio = 0.25f) ?: 1.0f
            } else {
                ringtoneDucker?.getRingVolumePercent() ?: 1.0f
            }

            // If sync is enabled, scale announcer speech volume by the ringtone volume percentage
            val speechVolume = if (syncRingtoneVolume) ringPercent else 1.0f
            Log.d(TAG, "Ringtone percent: $ringPercent, Speech volume set to: $speechVolume")

            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val loops = if (repeatCount <= 0) 1 else repeatCount
            for (i in 0 until loops) {
                val utteranceId = UUID.randomUUID().toString()
                pendingUtteranceId = utteranceId

                val params = Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_RING)
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, speechVolume)
                }

                _isSpeaking.value = true
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

                var elapsedMs = 0L
                val maxWaitMs = 12000L
                while (_isSpeaking.value && elapsedMs < maxWaitMs) {
                    delay(150)
                    elapsedMs += 150
                }

                if (i < loops - 1) {
                    delay(repeatDelayMs)
                }
            }

            // 3. After the announcement repetitions finish, restore ringtone volume
            if (duckRingtone) {
                ringtoneDucker?.restoreRingtone()
            }

            _isSpeaking.value = false
            releaseWakeLock()
            onFinish?.invoke()
        }
    }

    fun stop() {
        announcementJob?.cancel()
        announcementJob = null
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS", e)
        }
        // Always restore ringtone volume whenever speech is stopped
        ringtoneDucker?.restoreRingtone()
        _isSpeaking.value = false
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(30000L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wakeLock", e)
        }
    }
}
