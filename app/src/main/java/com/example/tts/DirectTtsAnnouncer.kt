package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.util.AnnouncementVolumeController
import com.example.util.DebugLog
import com.example.util.DeviceStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

object DirectTtsAnnouncer : TextToSpeech.OnInitListener {

    private const val TAG = "DirectTtsAnnouncer"

    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeController: AnnouncementVolumeController? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _availableLanguages = MutableStateFlow<List<Locale>>(emptyList())
    val availableLanguages: StateFlow<List<Locale>> = _availableLanguages.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var announcementJob: Job? = null
    private var pendingUtteranceId: String? = null
    private var currentPlayer: MediaPlayer? = null
    private val utteranceResults = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    private const val SYNTHESIS_TIMEOUT_MS = 8000L
    private const val SPEECH_TIMEOUT_MS = 12000L
    private const val AUDIO_CACHE_DIR = "announcements"
    private const val MAX_CACHED_ANNOUNCEMENTS = 100

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            val powerManager = appContext?.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CallAnnouncer::DirectTtsWakeLock")
                ?.apply { setReferenceCounted(false) }
            volumeController = AnnouncementVolumeController.getInstance(appContext!!)
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
                applyLanguage(engine, "")
                _availableLanguages.value = installedLanguages(engine)

                // Engine-level attributes override any per-utterance KEY_PARAM_STREAM
                engine.setAudioAttributes(AnnouncementVolumeController.ANNOUNCEMENT_ATTRIBUTES)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = true
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        utteranceResults.remove(utteranceId)?.complete(true)
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        utteranceResults.remove(utteranceId)?.complete(false)
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.w(TAG, "TTS utterance error: $errorCode on $utteranceId")
                        utteranceResults.remove(utteranceId)?.complete(false)
                        if (utteranceId == pendingUtteranceId) {
                            _isSpeaking.value = false
                        }
                    }
                })

                _isReady.value = true
                DebugLog.d(TAG, "TTS initialized and ready")
            }
        } else {
            Log.e(TAG, "TTS initialization failed code: $status")
            _isReady.value = false
        }
    }

    /** Synchronized so concurrent callers (UI and call handler) replace, rather than overlap, each other. */
    @Synchronized
    fun speak(
        context: Context,
        text: String,
        repeatCount: Int = 1,
        initialDelayMs: Long = 2000L,
        repeatDelayMs: Long = 1500L,
        rate: Float = 1.0f,
        pitch: Float = 1.0f,
        languageTag: String = "",
        duckRingtone: Boolean = true,
        syncRingtoneVolume: Boolean = true,
        skipWhenSilenced: Boolean = false,
        onFinish: (() -> Unit)? = null
    ): Job {
        initialize(context)
        stop()

        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                announce(text, repeatCount, initialDelayMs, repeatDelayMs, rate, pitch,
                    languageTag, duckRingtone, syncRingtoneVolume, skipWhenSilenced)
            } finally {
                // A replaced announcement must not release the wake lock of the one that replaced it
                synchronized(this@DirectTtsAnnouncer) {
                    if (announcementJob === coroutineContext[Job]) {
                        _isSpeaking.value = false
                        releaseWakeLock()
                    }
                }
                onFinish?.invoke()
            }
        }
        announcementJob = job
        job.start()
        return job
    }

    private suspend fun announce(
        text: String,
        repeatCount: Int,
        initialDelayMs: Long,
        repeatDelayMs: Long,
        rate: Float,
        pitch: Float,
        languageTag: String,
        duckRingtone: Boolean,
        syncRingtoneVolume: Boolean,
        skipWhenSilenced: Boolean
    ) {
        if (skipWhenSilenced && volumeController?.isRingerSilenced() == true) {
            DebugLog.d(TAG, "Phone is on silent/vibrate; skipping announcement")
            return
        }

        acquireWakeLock()

        // Wait briefly if initializing
        var waitCount = 0
        while (!_isReady.value && waitCount < 20) {
            delay(100)
            waitCount++
        }

        if (!_isReady.value || tts == null) {
            Log.w(TAG, "TTS not ready to speak announcement (ready=${_isReady.value})")
            return
        }

        tts?.let { applyLanguage(it, languageTag) }
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

        // 1. Prepare the audio during the initial delay, while the ringtone plays at normal volume.
        // Playing the audio from this process (instead of the TTS engine's) keeps the app
        // "playing audio", which stops OEM freezers like TECNO's Hiber from pausing it mid-speech.
        val startedAt = SystemClock.elapsedRealtime()
        val audioFile = obtainAudioFile(text, rate, pitch)
        DebugLog.d(TAG, "Audio ready in ${SystemClock.elapsedRealtime() - startedAt}ms, " +
            "file=${audioFile?.length() ?: "failed; using engine playback"}")
        val remainingDelay = initialDelayMs - (SystemClock.elapsedRealtime() - startedAt)
        if (remainingDelay > 0) {
            delay(remainingDelay)
        }

        // 2. Raise the announcement above the ringtone level and lower the ringtone
        volumeController?.begin(
            duckRingtone = duckRingtone,
            louderThanRingtone = syncRingtoneVolume
        )

        val loops = if (repeatCount <= 0) 1 else repeatCount
        for (i in 0 until loops) {
            _isSpeaking.value = true
            if (audioFile != null) {
                playFile(audioFile)
            } else {
                speakWithEngine(text)
            }

            if (i < loops - 1) {
                delay(repeatDelayMs)
            }
        }

        // 3. After the announcement repetitions finish, restore ringtone and announcement volumes
        volumeController?.restore()
    }

    /** Uses the cached recording for this exact announcement, synthesizing it on first use. */
    private suspend fun obtainAudioFile(text: String, rate: Float, pitch: Float): File? {
        val engine = tts ?: return null
        val context = appContext ?: return null
        val cacheDir = File(DeviceStorage.context(context).cacheDir, AUDIO_CACHE_DIR).apply { mkdirs() }
        val voiceName = try { engine.voice?.name } catch (_: Exception) { null }
        val key = sha256("$text|$rate|$pitch|$voiceName")
        val cached = File(cacheDir, "$key.wav")
        if (cached.length() > 0) {
            cached.setLastModified(System.currentTimeMillis())
            DebugLog.d(TAG, "Using cached announcement audio")
            return cached
        }

        val partial = File(cacheDir, "$key.tmp")
        val utteranceId = UUID.randomUUID().toString()
        val result = CompletableDeferred<Boolean>()
        utteranceResults[utteranceId] = result
        return try {
            if (engine.synthesizeToFile(text, Bundle(), partial, utteranceId) != TextToSpeech.SUCCESS) {
                return null
            }
            val ok = withTimeoutOrNull(SYNTHESIS_TIMEOUT_MS) { result.await() } ?: false
            if (ok && partial.length() > 0 && partial.renameTo(cached)) {
                trimAudioCache(cacheDir)
                cached
            } else {
                partial.delete()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to synthesize announcement to file", e)
            partial.delete()
            null
        } finally {
            utteranceResults.remove(utteranceId)
        }
    }

    private fun trimAudioCache(dir: File) {
        val files = dir.listFiles { file -> file.extension == "wav" } ?: return
        files.sortedByDescending { it.lastModified() }
            .drop(MAX_CACHED_ANNOUNCEMENTS)
            .forEach { it.delete() }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun applyLanguage(engine: TextToSpeech, languageTag: String) {
        val requested = if (languageTag.isBlank()) Locale.getDefault() else Locale.forLanguageTag(languageTag)
        val result = engine.setLanguage(requested)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Voice language $requested unavailable; falling back")
            val fallback = engine.setLanguage(Locale.getDefault())
            if (fallback == TextToSpeech.LANG_MISSING_DATA || fallback == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }
        }
    }

    /** Re-reads installed voices, e.g. after the user downloads one in Android settings. */
    fun refreshAvailableLanguages() {
        val engine = tts ?: return
        if (_isReady.value) _availableLanguages.value = installedLanguages(engine)
    }

    /** Languages with at least one voice installed on the phone (usable offline). */
    private fun installedLanguages(engine: TextToSpeech): List<Locale> = try {
        engine.voices.orEmpty()
            .filter { voice ->
                !voice.isNetworkConnectionRequired &&
                    TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
            }
            .map { Locale.forLanguageTag(it.locale.toLanguageTag()) }
            .distinctBy { it.toLanguageTag() }
            .sortedBy { it.getDisplayName(Locale.getDefault()) }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to list TTS voices", e)
        emptyList()
    }

    private suspend fun playFile(file: File) = suspendCancellableCoroutine { cont ->
        val player = MediaPlayer()
        currentPlayer = player
        val finish = {
            releasePlayer(player)
            if (cont.isActive) cont.resume(Unit)
        }
        try {
            player.setAudioAttributes(AnnouncementVolumeController.ANNOUNCEMENT_ATTRIBUTES)
            player.setDataSource(file.absolutePath)
            player.setOnCompletionListener { finish() }
            player.setOnErrorListener { _, what, extra ->
                Log.w(TAG, "MediaPlayer error $what/$extra")
                finish()
                true
            }
            player.prepare()
            player.start()
            DebugLog.d(TAG, "Playing announcement (${player.duration}ms)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play announcement file", e)
            finish()
        }
        cont.invokeOnCancellation { releasePlayer(player) }
    }

    private suspend fun speakWithEngine(text: String) {
        val utteranceId = UUID.randomUUID().toString()
        pendingUtteranceId = utteranceId
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        var elapsedMs = 0L
        while (_isSpeaking.value && elapsedMs < SPEECH_TIMEOUT_MS) {
            delay(150)
            elapsedMs += 150
        }
    }

    @Synchronized
    private fun releasePlayer(player: MediaPlayer) {
        try {
            player.release()
        } catch (_: Exception) {
        }
        if (currentPlayer === player) currentPlayer = null
    }

    @Synchronized
    fun stop() {
        announcementJob?.cancel()
        announcementJob = null
        currentPlayer?.let { releasePlayer(it) }
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS", e)
        }
        // Always restore volumes whenever speech is stopped
        volumeController?.restore()
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
