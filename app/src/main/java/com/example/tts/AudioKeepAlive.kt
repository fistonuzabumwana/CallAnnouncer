package com.example.tts

import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import android.util.Log
import com.example.util.DebugLog
import com.example.util.AnnouncementVolumeController

/**
 * Plays inaudible silence from this process while a call is being announced.
 * OEM app freezers (TECNO/Infinix "Hiber", etc.) skip apps that are actively playing audio,
 * so this keeps the announcer from being frozen between the call event and the speech.
 */
object AudioKeepAlive {

    private const val TAG = "AudioKeepAlive"
    private const val SAMPLE_RATE = 8000
    private const val MAX_DURATION_MS = 60_000L

    @Volatile
    private var running = false
    private var thread: Thread? = null

    @Synchronized
    fun start() {
        if (running) return
        running = true
        thread = Thread({ playSilence() }, "AudioKeepAlive").apply { start() }
        DebugLog.d(TAG, "Started")
    }

    @Synchronized
    fun stop() {
        if (!running) return
        running = false
        thread = null
        DebugLog.d(TAG, "Stopped")
    }

    private fun playSilence() {
        var track: AudioTrack? = null
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(SAMPLE_RATE / 5)
            track = AudioTrack.Builder()
                .setAudioAttributes(AnnouncementVolumeController.ANNOUNCEMENT_ATTRIBUTES)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(bufferSize)
                .build()
            track.play()

            val silence = ByteArray(bufferSize)
            val startedAt = SystemClock.elapsedRealtime()
            while (running && SystemClock.elapsedRealtime() - startedAt < MAX_DURATION_MS) {
                track.write(silence, 0, silence.size)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Silent keep-alive playback failed", e)
        } finally {
            try {
                track?.stop()
            } catch (_: Exception) {
            }
            track?.release()
            running = false
        }
    }
}
