package com.example.util

/** Chat apps whose new-message notifications can be announced. */
enum class MessagingApp(val label: String, val packages: Set<String>) {
    WHATSAPP("WhatsApp", setOf("com.whatsapp", "com.whatsapp.w4b")),
    TELEGRAM("Telegram", setOf("org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram")),
    SIGNAL("Signal", setOf("org.thoughtcrime.securesms")),
    MESSENGER("Messenger", setOf("com.facebook.orca", "com.facebook.mlite")),
    INSTAGRAM("Instagram", setOf("com.instagram.android")),
    VIBER("Viber", setOf("com.viber.voip")),
    SMS(
        "SMS",
        setOf(
            "com.google.android.apps.messaging", "com.android.mms", "com.android.messaging",
            "com.samsung.android.messaging", "com.transsion.message", "com.transsion.messaging"
        )
    );

    companion object {
        /** [smsPackage] is the phone's default SMS app, which may not be in the list above. */
        fun fromPackage(packageName: String, smsPackage: String? = null): MessagingApp? =
            if (packageName == smsPackage) SMS else entries.firstOrNull { packageName in it.packages }
    }
}

/** What a message notification says, without Android types so it can be unit tested. */
data class MessageNotification(
    val packageName: String,
    val category: String?,
    val isGroupSummary: Boolean,
    val isOngoing: Boolean,
    val title: String?,
    val text: String?,
    /** MessagingStyle: the conversation (group) name, and the newest message and its sender. */
    val conversationTitle: String? = null,
    val isGroupConversation: Boolean = false,
    val lastSender: String? = null,
    val lastText: String? = null
)

data class IncomingMessage(val app: MessagingApp, val sender: String, val group: String?, val text: String?)

object MessageParser {

    private const val CATEGORY_MESSAGE = "msg"

    /** Status notifications of chat apps that are not messages. */
    private val notMessages = Regex(
        "(?i)^checking for new messages|^\\d+ (new )?messages?$|^\\d+ messages from \\d+ chats|" +
            "^(backup|sending|uploading|downloading) |^(missed|incoming|ongoing) (voice |video )?call|" +
            "^(calling|ringing)…?$|web is currently active"
    )
    private val countSuffix = Regex("\\s*\\(\\d+ (new )?messages?\\)$", RegexOption.IGNORE_CASE)

    fun parse(n: MessageNotification, smsPackage: String? = null): IncomingMessage? {
        val app = MessagingApp.fromPackage(n.packageName, smsPackage) ?: return null
        if (n.isGroupSummary || n.isOngoing) return null
        val hasMessage = n.lastText != null || n.category == CATEGORY_MESSAGE
        if (!hasMessage) return null
        if (n.category == "call") return null

        val text = (n.lastText ?: n.text)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (notMessages.containsMatchIn(text)) return null

        val group = n.conversationTitle?.trim()?.takeIf { n.isGroupConversation && it.isNotEmpty() }
            ?.replace(countSuffix, "")
        val sender = cleanName(n.lastSender ?: n.title?.let { title ->
            // Older group notifications: "Family: Jean" or "Jean @ Family"
            if (group == null) title.substringAfter(": ", title) else title
        }) ?: return null
        if (sender.equals(app.label, ignoreCase = true) || notMessages.containsMatchIn(sender)) return null
        return IncomingMessage(app, sender, group?.takeIf { it != sender }, text)
    }

    private fun cleanName(raw: String?): String? =
        raw?.replace(countSuffix, "")?.trim()?.removePrefix("~")?.trim()?.takeIf { it.isNotEmpty() }
}
