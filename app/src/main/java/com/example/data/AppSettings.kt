package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.PairSpokenMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AnnouncerSettings(
    val isEnabled: Boolean = true,
    val prefixText: String = "Incoming call from",
    val pairMode: PairSpokenMode = PairSpokenMode.PAIRED_NUMBERS,
    val repeatCount: Int = 2,
    val initialDelayMs: Long = 2000L,
    val repeatDelayMs: Long = 1500L,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val announceContacts: Boolean = true,
    val announceUnknowns: Boolean = true,
    val duckRingtone: Boolean = true,
    val syncRingtoneVolume: Boolean = true
)

class AppSettings private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AnnouncerSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AnnouncerSettings {
        val isEnabled = prefs.getBoolean(KEY_IS_ENABLED, true)
        val prefix = prefs.getString(KEY_PREFIX, "Incoming call from") ?: "Incoming call from"
        val pairModeName = prefs.getString(KEY_PAIR_MODE, PairSpokenMode.PAIRED_NUMBERS.name)
        val pairMode = try {
            PairSpokenMode.valueOf(pairModeName ?: PairSpokenMode.PAIRED_NUMBERS.name)
        } catch (_: Exception) {
            PairSpokenMode.PAIRED_NUMBERS
        }
        val repeatCount = prefs.getInt(KEY_REPEAT_COUNT, 2)
        val initialDelay = prefs.getLong(KEY_INITIAL_DELAY, 1000L)
        val repeatDelay = prefs.getLong(KEY_REPEAT_DELAY, 1500L)
        val speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        val speechPitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        val announceContacts = prefs.getBoolean(KEY_ANNOUNCE_CONTACTS, true)
        val announceUnknowns = prefs.getBoolean(KEY_ANNOUNCE_UNKNOWNS, true)
        val duckRingtone = prefs.getBoolean(KEY_DUCK_RINGTONE, true)
        val syncRingtone = prefs.getBoolean(KEY_SYNC_RINGTONE, true)

        return AnnouncerSettings(
            isEnabled = isEnabled,
            prefixText = prefix,
            pairMode = pairMode,
            repeatCount = repeatCount,
            initialDelayMs = initialDelay,
            repeatDelayMs = repeatDelay,
            speechRate = speechRate,
            speechPitch = speechPitch,
            announceContacts = announceContacts,
            announceUnknowns = announceUnknowns,
            duckRingtone = duckRingtone,
            syncRingtoneVolume = syncRingtone
        )
    }

    fun updateSettings(transform: (AnnouncerSettings) -> AnnouncerSettings) {
        val current = _settingsFlow.value
        val updated = transform(current)
        prefs.edit()
            .putBoolean(KEY_IS_ENABLED, updated.isEnabled)
            .putString(KEY_PREFIX, updated.prefixText)
            .putString(KEY_PAIR_MODE, updated.pairMode.name)
            .putInt(KEY_REPEAT_COUNT, updated.repeatCount)
            .putLong(KEY_INITIAL_DELAY, updated.initialDelayMs)
            .putLong(KEY_REPEAT_DELAY, updated.repeatDelayMs)
            .putFloat(KEY_SPEECH_RATE, updated.speechRate)
            .putFloat(KEY_SPEECH_PITCH, updated.speechPitch)
            .putBoolean(KEY_ANNOUNCE_CONTACTS, updated.announceContacts)
            .putBoolean(KEY_ANNOUNCE_UNKNOWNS, updated.announceUnknowns)
            .putBoolean(KEY_DUCK_RINGTONE, updated.duckRingtone)
            .putBoolean(KEY_SYNC_RINGTONE, updated.syncRingtoneVolume)
            .apply()

        _settingsFlow.value = updated
    }

    fun getSettings(): AnnouncerSettings = _settingsFlow.value

    companion object {
        private const val PREFS_NAME = "call_announcer_prefs"
        private const val KEY_IS_ENABLED = "is_enabled"
        private const val KEY_PREFIX = "prefix"
        private const val KEY_PAIR_MODE = "pair_mode"
        private const val KEY_REPEAT_COUNT = "repeat_count"
        private const val KEY_INITIAL_DELAY = "initial_delay"
        private const val KEY_REPEAT_DELAY = "repeat_delay"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_ANNOUNCE_CONTACTS = "announce_contacts"
        private const val KEY_ANNOUNCE_UNKNOWNS = "announce_unknowns"
        private const val KEY_DUCK_RINGTONE = "duck_ringtone"
        private const val KEY_SYNC_RINGTONE = "sync_ringtone"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                val instance = AppSettings(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
