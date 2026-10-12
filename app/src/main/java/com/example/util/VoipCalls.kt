package com.example.util

/** Internet calling apps whose incoming-call notifications can be announced. */
enum class CallingApp(val label: String, val isWhatsApp: Boolean, val packages: Set<String>) {
    WHATSAPP("WhatsApp", true, setOf("com.whatsapp")),
    WHATSAPP_BUSINESS("WhatsApp Business", true, setOf("com.whatsapp.w4b")),
    TELEGRAM("Telegram", false, setOf("org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram")),
    SIGNAL("Signal", false, setOf("org.thoughtcrime.securesms")),
    MESSENGER("Messenger", false, setOf("com.facebook.orca", "com.facebook.mlite")),
    INSTAGRAM("Instagram", false, setOf("com.instagram.android")),
    TEAMS("Teams", false, setOf("com.microsoft.teams")),
    GOOGLE_MEET("Google Meet", false, setOf("com.google.android.apps.tachyon")),
    VIBER("Viber", false, setOf("com.viber.voip")),
    SKYPE("Skype", false, setOf("com.skype.raider")),
    LINE("LINE", false, setOf("jp.naver.line.android")),
    DISCORD("Discord", false, setOf("com.discord")),
    ZOOM("Zoom", false, setOf("us.zoom.videomeetings")),
    IMO("imo", false, setOf("com.imo.android.imoim")),
    BOTIM("BOTIM", false, setOf("im.thebot.messenger"));

    companion object {
        fun fromPackage(packageName: String): CallingApp? = entries.firstOrNull { packageName in it.packages }
    }
}

/** What an internet-call notification says, read from the notification without Android types so it can be unit tested. */
data class CallNotification(
    val packageName: String,
    val category: String?,
    /** Notification.CallStyle call type (1 incoming, 2 ongoing, 3 screening); 0 when not set. */
    val callStyleType: Int,
    val hasFullScreenIntent: Boolean,
    val isOngoingFlag: Boolean,
    val callPersonName: String?,
    val title: String?,
    val text: String?
)

data class IncomingVoipCall(
    val app: CallingApp,
    /** Name or number as the calling app shows it; null when it shows none. */
    val caller: String?,
    val isVideo: Boolean,
    /** The app marked the caller as someone not saved in the phone's contacts. */
    val isUnsavedProfile: Boolean = false
)

sealed interface VoipCallState {
    data class Ringing(val call: IncomingVoipCall) : VoipCallState
    /** A call notification that no longer rings (answered, ongoing or ended). */
    data object NotRinging : VoipCallState
    /** Not a call notification from a supported app. */
    data object Unrelated : VoipCallState
}

object VoipCallParser {

    private const val CATEGORY_CALL = "call"
    private const val CALL_TYPE_INCOMING = 1

    private val incomingWords = Regex(
        "(?i)incoming|ringing|is calling|calling you|entrant|appel de|sonne|eingehend|entrante|llamada de|em chamada|recebida|in arrivo|binnenkomend|входящ|وارد|gelen|masuk|inayoingia|kwinjira"
    )
    private val ongoingWords = Regex("(?i)ongoing|in call|on call|en cours|connecting|ended|missed|manqué|^calling|^appel en cours")
    private val videoWords = Regex("(?i)vid[eé]o|vídeo|видео")
    private val phoneLike = Regex("^[+\\d][\\d\\s().-]{5,}$")

    fun parse(n: CallNotification): VoipCallState {
        val app = CallingApp.fromPackage(n.packageName) ?: return VoipCallState.Unrelated
        val isCallNotification = n.category == CATEGORY_CALL || n.callStyleType != 0
        if (!isCallNotification) return VoipCallState.Unrelated

        val text = listOfNotNull(n.title, n.text).joinToString(" ")
        val ringing = when {
            n.callStyleType != 0 -> n.callStyleType == CALL_TYPE_INCOMING
            n.hasFullScreenIntent -> true
            ongoingWords.containsMatchIn(n.text.orEmpty().trim()) -> false
            else -> incomingWords.containsMatchIn(text)
        }
        if (!ringing) return VoipCallState.NotRinging

        return VoipCallState.Ringing(
            IncomingVoipCall(
                app = app,
                caller = callerName(n, app),
                isVideo = videoWords.containsMatchIn(text),
                isUnsavedProfile = isUnsavedProfileName(n.callPersonName ?: n.title)
            )
        )
    }

    private fun callerName(n: CallNotification, app: CallingApp): String? {
        val candidate = n.callPersonName?.takeIf { it.isNotBlank() } ?: n.title
        val name = candidate
            ?.trim()
            // WhatsApp marks people who are not in your contacts with "~"
            ?.removePrefix("~")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        // Some apps put their own name or a generic phrase in the title instead of the caller
        if (name.equals(app.label, ignoreCase = true)) return null
        if (incomingWords.containsMatchIn(name) && name.split(" ").size > 2) return null
        return name
    }

    fun looksLikePhoneNumber(value: String): Boolean = phoneLike.matches(value.trim())

    /** WhatsApp shows "~ Name" for people who are not saved in the phone's contacts. */
    fun isUnsavedProfileName(title: String?): Boolean = title?.trim()?.startsWith("~") == true
}
