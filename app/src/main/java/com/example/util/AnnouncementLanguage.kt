package com.example.util

/** Language of the fixed words around the caller ("Incoming call from", "on WhatsApp"…). */
enum class AnnouncementLanguage(
    val label: String,
    /** Voice language the phrases are written for. */
    val languageTag: String,
    val defaultPrefix: String,
    val prefixSuggestions: List<String>,
    val unknownCaller: String,
    private val onApp: String,
    private val videoCallOnApp: String,
    val possibleSpam: String,
    private val networkNumber: String,
    val messageFrom: String,
    private val inGroup: String,
    val missedOneCall: String,
    val missedCalls: String,
    val and: String
) {
    ENGLISH(
        label = "English",
        languageTag = "en",
        defaultPrefix = "Incoming call from",
        prefixSuggestions = listOf("Incoming call from", "Call from", "Call alert:", "Phone call from"),
        unknownCaller = "unknown caller",
        onApp = ", on %s",
        videoCallOnApp = ", %s video call",
        possibleSpam = "Possible spam.",
        networkNumber = "an %s number,",
        messageFrom = "Message from",
        inGroup = "in %s",
        missedOneCall = "You missed a call from",
        missedCalls = "You missed calls from",
        and = "and"
    ),
    KINYARWANDA(
        label = "Kinyarwanda",
        languageTag = "rw",
        defaultPrefix = "Uguhamagara ni",
        prefixSuggestions = listOf("Uguhamagara ni", "Umuhamagaro wa", "Urahamagarwa na"),
        unknownCaller = "nimero itazwi",
        onApp = ", kuri %s",
        videoCallOnApp = ", videwo kuri %s",
        possibleSpam = "Witondere, bishobora kuba ari ubutekamutwe.",
        networkNumber = "nimero ya %s,",
        messageFrom = "Ubutumwa bwa",
        inGroup = "muri %s",
        missedOneCall = "Ntiwitabye umuhamagaro wa",
        missedCalls = "Ntiwitabye imihamagaro ya",
        and = "na"
    ),
    FRENCH(
        label = "Français",
        languageTag = "fr",
        defaultPrefix = "Appel de",
        prefixSuggestions = listOf("Appel de", "Appel entrant de", "Vous avez un appel de"),
        unknownCaller = "numéro inconnu",
        onApp = ", sur %s",
        videoCallOnApp = ", appel vidéo %s",
        possibleSpam = "Attention, appel suspect.",
        networkNumber = "un numéro %s,",
        messageFrom = "Message de",
        inGroup = "dans %s",
        missedOneCall = "Vous avez manqué un appel de",
        missedCalls = "Vous avez manqué des appels de",
        and = "et"
    );

    fun appSuffix(appLabel: String, isVideo: Boolean): String =
        (if (isVideo) videoCallOnApp else onApp).format(appLabel)

    fun network(name: String): String = networkNumber.format(name)

    fun group(name: String): String = inGroup.format(name)

    companion object {
        val ALL_PREFIXES: Set<String> = entries.flatMap { it.prefixSuggestions }.toSet()

        fun fromName(name: String?): AnnouncementLanguage =
            entries.firstOrNull { it.name == name } ?: ENGLISH
    }
}
