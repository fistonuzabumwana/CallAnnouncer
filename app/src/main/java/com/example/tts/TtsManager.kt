package com.example.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val tag = "TtsManager"
    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastAnnouncedText = MutableStateFlow<String?>(null)
    val lastAnnouncedText: StateFlow<String?> = _lastAnnouncedText.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var currentAnnouncementJob: Job? = null
    private var pendingUtteranceId: String? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(tag, "Failed to instantiate TextToSpeech", e)
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
                        Log.w(tag, "TTS utterance error: $errorCode on $utteranceId")
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }
                })

                _isReady.value = true
                Log.d(tag, "TTS engine initialized successfully")
            }
        } else {
            Log.e(tag, "TTS initialization failed with code: $status")
            _isReady.value = false
        }
    }

    /**
     * Speaks an announcement once or repeatedly with configured intervals.
     */
    fun speakAnnouncement(
        text: String,
        repeatCount: Int = 1,
        initialDelayMs: Long = 500L,
        repeatDelayMs: Long = 1500L,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        onFinish: (() -> Unit)? = null
    ) {
        stop()

        currentAnnouncementJob = scope.launch {
            if (!_isReady.value) {
                // Wait briefly for TTS initialization if needed
                var attempts = 0
                while (!_isReady.value && attempts < 15) {
                    delay(150)
                    attempts++
                }
            }

            if (!_isReady.value || tts == null) {
                Log.w(tag, "TTS engine not ready to speak")
                return@launch
            }

            _lastAnnouncedText.value = text

            if (initialDelayMs > 0) {
                delay(initialDelayMs)
            }

            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val loops = if (repeatCount <= 0) 1 else repeatCount
            for (i in 0 until loops) {
                val utteranceId = UUID.randomUUID().toString()
                pendingUtteranceId = utteranceId

                val params = Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_RING)
                }

                _isSpeaking.value = true
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

                // Wait until current utterance finishes or times out
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

            _isSpeaking.value = false
            onFinish?.invoke()
        }
    }

    /**
     * Immediately stops ongoing speech and cancels any scheduled repetitions.
     */
    fun stop() {
        currentAnnouncementJob?.cancel()
        currentAnnouncementJob = null
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(tag, "Error stopping TTS", e)
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
            _isReady.value = false
        } catch (e: Exception) {
            Log.w(tag, "Error shutting down TTS", e)
        }
    }
}
