package com.example.util

/** One piece of an announcement; each kind may be read by a different voice. */
data class SpokenPart(val text: String, val kind: Kind) {
    /** SPELLED is a name written the way it should sound; it is read as-is by the main voice. */
    enum class Kind { PHRASE, NAME, SPELLED, NUMBER }
}

data class AnnouncementResult(
    val displayText: String,
    val spokenText: String,
    val callerIdentifier: String,
    val isKnownContact: Boolean,
    val hasMtnCarrier: Boolean = false,
    val parts: List<SpokenPart> = emptyList(),
    val language: AnnouncementLanguage = AnnouncementLanguage.ENGLISH
)

object AnnouncementBuilder {

    /**
     * Sanitizes contact names and speech phrases for Text-To-Speech.
     * Exception: Whenever "MTN" appears (in any case: MTN, mtn, Mtn, mTn, etc.),
     * it ensures TTS spells out the letters "M T N" (Em Tee En) instead of
     * expanding the abbreviation to "mountain".
     */
    fun sanitizeMtnForSpeech(input: String): String {
        if (input.isBlank()) return input

        // 1. Standalone word with word boundaries: e.g. "Yves MTN", "Yves mtn"
        var processed = input.replace(Regex("(?i)\\bmtn\\b"), "M T N")

        // 2. Attached as suffix without space: e.g. "YvesMTN" -> "Yves M T N"
        processed = processed.replace(Regex("(?<=[a-zA-Z0-9])(?i)mtn\\b"), " M T N")

        // 3. Attached as prefix without space: e.g. "MTNYves" -> "M T N Yves"
        processed = processed.replace(Regex("\\b(?i)mtn(?=[a-zA-Z0-9])"), "M T N ")

        // 4. Underscore or hyphen separated: e.g. "Yves_mtn" or "Yves-mtn"
        processed = processed.replace(Regex("(?i)[_-]mtn[_-]"), " M T N ")
        processed = processed.replace(Regex("(?i)[_-]mtn\\b"), " M T N")
        processed = processed.replace(Regex("(?i)\\bmtn[_-]"), "M T N ")

        // Clean up multiple spaces
        return processed.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Checks if the text contains the letters "mtn" (case-insensitive).
     */
    fun containsMtn(input: String?): Boolean {
        if (input.isNullOrBlank()) return false
        return input.contains(Regex("(?i)\\bmtn\\b")) || input.contains(Regex("(?i)mtn"))
    }

    /**
     * Constructs the announcement payload.
     * If a contact name is found, announces the contact name (e.g. "Call from Yves MTN").
     * "MTN" in contact names is pronounced letter-by-letter as "M T N", never as "mountain".
     * If no contact name, announces the number grouped in pairs of two (e.g. "Call from 07, 85, 46"),
     * optionally preceded by its network ("Call from an MTN number, 07, 88…").
     *
     * [spokenName] replaces how the name is read (a pronunciation fix) without changing what is shown.
     */
    fun buildAnnouncement(
        contactName: String?,
        phoneNumber: String?,
        prefix: String = "Incoming call from",
        pairMode: PairSpokenMode = PairSpokenMode.PAIRED_NUMBERS,
        language: AnnouncementLanguage = AnnouncementLanguage.ENGLISH,
        sayNetwork: Boolean = false,
        possibleSpam: Boolean = false,
        callingApp: String? = null,
        isVideo: Boolean = false,
        spokenName: String? = null
    ): AnnouncementResult {
        val cleanPrefix = prefix.trim().ifEmpty { language.defaultPrefix }
        val parts = mutableListOf<SpokenPart>()
        val display = StringBuilder()

        if (possibleSpam) {
            parts += phrase(language.possibleSpam)
            display.append(language.possibleSpam).append(' ')
        }
        parts += phrase(cleanPrefix)
        display.append(cleanPrefix)

        val name = contactName?.trim()?.takeIf { it.isNotEmpty() }
        val number = phoneNumber?.trim()?.takeIf { it.isNotEmpty() }
        val callerIdentifier: String
        var hasMtn = false

        when {
            name != null -> {
                hasMtn = containsMtn(name)
                val spelled = spokenName?.trim()?.takeIf { it.isNotEmpty() }
                parts += if (spelled != null) SpokenPart(sanitizeMtnForSpeech(spelled), SpokenPart.Kind.SPELLED)
                else SpokenPart(sanitizeMtnForSpeech(name), SpokenPart.Kind.NAME)
                display.append(' ').append(name)
                callerIdentifier = name
            }
            number != null -> {
                val paired = PhoneNumberFormatter.formatInPairs(number)
                val network = if (sayNetwork) CallerNetwork.of(number) else null
                if (network != null) parts += phrase(language.network(network))
                parts += SpokenPart(PhoneNumberFormatter.formatForSpeech(number, pairMode), SpokenPart.Kind.NUMBER)
                display.append(' ').append(paired)
                if (network != null) display.append(" ($network)")
                callerIdentifier = paired
            }
            else -> {
                parts += phrase(language.unknownCaller)
                display.append(" Unknown Caller")
                callerIdentifier = "Unknown Caller"
            }
        }

        if (callingApp != null) {
            val suffix = language.appSuffix(callingApp, isVideo)
            parts += phrase(suffix)
            display.append(suffix)
        }

        return AnnouncementResult(
            displayText = display.toString(),
            spokenText = joinParts(parts),
            callerIdentifier = callerIdentifier,
            isKnownContact = name != null,
            hasMtnCarrier = hasMtn,
            parts = parts,
            language = language
        )
    }

    /** "Message from Jean in Family, on WhatsApp: see you at 5". */
    fun buildMessageAnnouncement(
        sender: String,
        appLabel: String,
        language: AnnouncementLanguage,
        group: String? = null,
        messageText: String? = null
    ): AnnouncementResult {
        val parts = mutableListOf(phrase(language.messageFrom), SpokenPart(sanitizeMtnForSpeech(sender), SpokenPart.Kind.NAME))
        if (group != null) parts += phrase(language.group(group))
        parts += phrase(language.appSuffix(appLabel, isVideo = false))
        val text = messageText?.trim()?.take(MAX_MESSAGE_CHARS)?.takeIf { it.isNotEmpty() }
        if (text != null) {
            parts[parts.lastIndex] = parts.last().copy(text = parts.last().text + ".")
            parts += SpokenPart(text, SpokenPart.Kind.SPELLED)
        }
        val spoken = joinParts(parts)
        return AnnouncementResult(
            displayText = spoken,
            spokenText = spoken,
            callerIdentifier = sender,
            isKnownContact = true,
            parts = parts,
            language = language
        )
    }

    /** "You missed calls from Jean, Paul and 07 88 12 34 56". */
    fun buildMissedCallsSummary(callers: List<String>, language: AnnouncementLanguage): AnnouncementResult {
        val parts = mutableListOf(phrase(if (callers.size == 1) language.missedOneCall else language.missedCalls))
        callers.forEachIndexed { index, caller ->
            when {
                index == 0 -> Unit
                index == callers.lastIndex -> parts += phrase(language.and)
                else -> parts += SpokenPart(",", SpokenPart.Kind.PHRASE)
            }
            val isNumber = caller.any { it.isDigit() } && caller.none { it.isLetter() }
            parts += if (isNumber) SpokenPart(PhoneNumberFormatter.formatForSpeech(caller), SpokenPart.Kind.NUMBER)
            else SpokenPart(sanitizeMtnForSpeech(caller), SpokenPart.Kind.NAME)
        }
        val spoken = joinParts(parts)
        return AnnouncementResult(
            displayText = spoken,
            spokenText = spoken,
            callerIdentifier = callers.joinToString(", "),
            isKnownContact = false,
            parts = parts,
            language = language
        )
    }

    private const val MAX_MESSAGE_CHARS = 200

    /** Joins parts into one sentence; a part starting with a comma attaches to the previous word. */
    fun joinParts(parts: List<SpokenPart>): String = buildString {
        parts.forEach { part ->
            if (isNotEmpty() && !part.text.startsWith(",")) append(' ')
            append(part.text)
        }
    }

    private fun phrase(text: String) = SpokenPart(sanitizeMtnForSpeech(text), SpokenPart.Kind.PHRASE)
}
