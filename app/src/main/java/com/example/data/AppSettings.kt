package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.i18n.AppLanguage
import com.example.i18n.I18n
import com.example.util.AnnouncementLanguage
import com.example.util.DeviceStorage
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
    val syncRingtoneVolume: Boolean = true,
    /** BCP-47 language tag for the announcement voice; empty means the phone's language. */
    val voiceLanguage: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Announce WhatsApp and WhatsApp Business calls (read from their call notifications). */
    val announceWhatsApp: Boolean = true,
    /** Announce calls from other internet calling apps (Telegram, Signal, Messenger…). */
    val announceOtherApps: Boolean = true,
    /** Speak Rwandan names with a Swahili voice (or respelled) and French names with a French voice. */
    val smartNamePronunciation: Boolean = true,
    /** Version of the privacy policy the user accepted; 0 means never accepted. */
    val acceptedPrivacyVersion: Int = 0,
    /** Language of the fixed words ("Incoming call from", "on WhatsApp", "unknown caller"…). */
    val announcementLanguage: AnnouncementLanguage = AnnouncementLanguage.ENGLISH,
    /** Say "an MTN number" / "an Airtel number" before unsaved Rwandan numbers. */
    val sayNetwork: Boolean = true,
    /** Start with "Possible spam" for numbers marked as spam or blocked before. */
    val spamWarning: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    /** Minutes after midnight. */
    val quietStartMinutes: Int = 22 * 60,
    val quietEndMinutes: Int = 7 * 60,
    /** Stop speaking when the ringtone is silenced with the volume or power button. */
    val stopWhenSilenced: Boolean = true,
    /** Stop speaking when the phone is turned face down. */
    val flipToStop: Boolean = true,
    /** Stop speaking when the phone is shaken. */
    val shakeToStop: Boolean = false,
    val headphoneMode: HeadphoneMode = HeadphoneMode.SPEAKER_AND_HEADPHONES,
    /** Announce new messages from WhatsApp, SMS and other chat apps. */
    val announceMessages: Boolean = false,
    /** Also read the message text, not just who sent it. */
    val readMessageText: Boolean = false,
    /** When the phone is unlocked after missed calls, say who called. */
    val missedCallSummary: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM
) {
    val hasAcceptedPrivacyPolicy: Boolean get() = acceptedPrivacyVersion >= PRIVACY_POLICY_VERSION

    companion object {
        /** Bump when the policy text changes materially so users are asked again. */
        const val PRIVACY_POLICY_VERSION = 3
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Where announcements play while headphones or a car are connected. */
enum class HeadphoneMode(val label: String) {
    SPEAKER_AND_HEADPHONES("Speaker and headphones"),
    HEADPHONES_ONLY("Only in headphones / car"),
    SILENT("Don't announce")
}

class AppSettings private constructor(context: Context) {

    private val lock = Any()
    private var prefs: SharedPreferences
    private val _settingsFlow: MutableStateFlow<AnnouncerSettings>
    val settingsFlow: StateFlow<AnnouncerSettings>

    init {
        // The unlock callback runs on the main thread and may fire while this constructor runs elsewhere
        synchronized(lock) {
            prefs = DeviceStorage.prefs(context, PREFS_NAME) { migrated ->
                synchronized(lock) {
                    prefs = migrated
                    publish(loadSettings())
                }
            }
            _settingsFlow = MutableStateFlow(loadSettings())
            settingsFlow = _settingsFlow.asStateFlow()
            I18n.apply(_settingsFlow.value.appLanguage)
        }
    }

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
        val voiceLanguage = prefs.getString(KEY_VOICE_LANGUAGE, "") ?: ""
        val themeMode = ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME_MODE, null) }
            ?: ThemeMode.SYSTEM
        val acceptedPrivacyVersion = prefs.getInt(KEY_PRIVACY_VERSION, 0)
        val announceWhatsApp = prefs.getBoolean(KEY_ANNOUNCE_WHATSAPP, true)
        val announceOtherApps = prefs.getBoolean(KEY_ANNOUNCE_OTHER_APPS, true)
        val smartNamePronunciation = prefs.getBoolean(KEY_SMART_NAMES, true)
        val headphoneMode = HeadphoneMode.entries.firstOrNull { it.name == prefs.getString(KEY_HEADPHONE_MODE, null) }
            ?: HeadphoneMode.SPEAKER_AND_HEADPHONES

        return AnnouncerSettings(
            announcementLanguage = AnnouncementLanguage.fromName(prefs.getString(KEY_ANNOUNCEMENT_LANGUAGE, null)),
            sayNetwork = prefs.getBoolean(KEY_SAY_NETWORK, true),
            spamWarning = prefs.getBoolean(KEY_SPAM_WARNING, true),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_ENABLED, false),
            quietStartMinutes = prefs.getInt(KEY_QUIET_START, 22 * 60),
            quietEndMinutes = prefs.getInt(KEY_QUIET_END, 7 * 60),
            stopWhenSilenced = prefs.getBoolean(KEY_STOP_WHEN_SILENCED, true),
            flipToStop = prefs.getBoolean(KEY_FLIP_TO_STOP, true),
            shakeToStop = prefs.getBoolean(KEY_SHAKE_TO_STOP, false),
            headphoneMode = headphoneMode,
            announceMessages = prefs.getBoolean(KEY_ANNOUNCE_MESSAGES, false),
            readMessageText = prefs.getBoolean(KEY_READ_MESSAGE_TEXT, false),
            missedCallSummary = prefs.getBoolean(KEY_MISSED_SUMMARY, false),
            appLanguage = AppLanguage.fromName(prefs.getString(KEY_APP_LANGUAGE, null)),
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
            syncRingtoneVolume = syncRingtone,
            voiceLanguage = voiceLanguage,
            themeMode = themeMode,
            acceptedPrivacyVersion = acceptedPrivacyVersion,
            announceWhatsApp = announceWhatsApp,
            announceOtherApps = announceOtherApps,
            smartNamePronunciation = smartNamePronunciation
        )
    }

    fun updateSettings(transform: (AnnouncerSettings) -> AnnouncerSettings) = synchronized(lock) {
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
            .putString(KEY_VOICE_LANGUAGE, updated.voiceLanguage)
            .putString(KEY_THEME_MODE, updated.themeMode.name)
            .putInt(KEY_PRIVACY_VERSION, updated.acceptedPrivacyVersion)
            .putBoolean(KEY_ANNOUNCE_WHATSAPP, updated.announceWhatsApp)
            .putBoolean(KEY_ANNOUNCE_OTHER_APPS, updated.announceOtherApps)
            .putBoolean(KEY_SMART_NAMES, updated.smartNamePronunciation)
            .putString(KEY_ANNOUNCEMENT_LANGUAGE, updated.announcementLanguage.name)
            .putBoolean(KEY_SAY_NETWORK, updated.sayNetwork)
            .putBoolean(KEY_SPAM_WARNING, updated.spamWarning)
            .putBoolean(KEY_QUIET_ENABLED, updated.quietHoursEnabled)
            .putInt(KEY_QUIET_START, updated.quietStartMinutes)
            .putInt(KEY_QUIET_END, updated.quietEndMinutes)
            .putBoolean(KEY_STOP_WHEN_SILENCED, updated.stopWhenSilenced)
            .putBoolean(KEY_FLIP_TO_STOP, updated.flipToStop)
            .putBoolean(KEY_SHAKE_TO_STOP, updated.shakeToStop)
            .putString(KEY_HEADPHONE_MODE, updated.headphoneMode.name)
            .putBoolean(KEY_ANNOUNCE_MESSAGES, updated.announceMessages)
            .putBoolean(KEY_READ_MESSAGE_TEXT, updated.readMessageText)
            .putBoolean(KEY_MISSED_SUMMARY, updated.missedCallSummary)
            .putString(KEY_APP_LANGUAGE, updated.appLanguage.name)
            .apply()

        publish(updated)
    }

    private fun publish(settings: AnnouncerSettings) {
        I18n.apply(settings.appLanguage)
        _settingsFlow.value = settings
    }

    fun getSettings(): AnnouncerSettings = _settingsFlow.value

    /** Re-reads the stored values, e.g. after a backup was restored. */
    fun reload() = synchronized(lock) {
        publish(loadSettings())
    }

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
        private const val KEY_VOICE_LANGUAGE = "voice_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PRIVACY_VERSION = "accepted_privacy_version"
        private const val KEY_ANNOUNCE_WHATSAPP = "announce_whatsapp"
        private const val KEY_ANNOUNCE_OTHER_APPS = "announce_other_apps"
        private const val KEY_SMART_NAMES = "smart_name_pronunciation"
        private const val KEY_ANNOUNCEMENT_LANGUAGE = "announcement_language"
        private const val KEY_SAY_NETWORK = "say_network"
        private const val KEY_SPAM_WARNING = "spam_warning"
        private const val KEY_QUIET_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_START = "quiet_start_minutes"
        private const val KEY_QUIET_END = "quiet_end_minutes"
        private const val KEY_STOP_WHEN_SILENCED = "stop_when_silenced"
        private const val KEY_FLIP_TO_STOP = "flip_to_stop"
        private const val KEY_SHAKE_TO_STOP = "shake_to_stop"
        private const val KEY_HEADPHONE_MODE = "headphone_mode"
        private const val KEY_ANNOUNCE_MESSAGES = "announce_messages"
        private const val KEY_READ_MESSAGE_TEXT = "read_message_text"
        private const val KEY_MISSED_SUMMARY = "missed_call_summary"
        private const val KEY_APP_LANGUAGE = "app_language"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
