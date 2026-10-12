package com.example.util

data class AnnouncementResult(
    val displayText: String,
    val spokenText: String,
    val callerIdentifier: String,
    val isKnownContact: Boolean,
    val hasMtnCarrier: Boolean = false
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
     * If no contact name, announces the number grouped in pairs of two (e.g. "Call from 07, 85, 46").
     */
    fun buildAnnouncement(
        contactName: String?,
        phoneNumber: String?,
        prefix: String = "Incoming call from",
        pairMode: PairSpokenMode = PairSpokenMode.PAIRED_NUMBERS
    ): AnnouncementResult {
        val cleanPrefix = prefix.trim().ifEmpty { "Call from" }
        val spokenPrefix = sanitizeMtnForSpeech(cleanPrefix)

        val hasValidContact = !contactName.isNullOrBlank()
        val hasValidNumber = !phoneNumber.isNullOrBlank()

        if (hasValidContact) {
            val name = contactName!!.trim()
            val hasMtn = containsMtn(name)
            val spokenName = sanitizeMtnForSpeech(name)

            val display = "$cleanPrefix $name"
            val spoken = "$spokenPrefix $spokenName"

            return AnnouncementResult(
                displayText = display,
                spokenText = spoken,
                callerIdentifier = name,
                isKnownContact = true,
                hasMtnCarrier = hasMtn
            )
        }

        if (hasValidNumber) {
            val number = phoneNumber!!.trim()
            val pairedDisplay = PhoneNumberFormatter.formatInPairs(number)
            val pairedSpeech = PhoneNumberFormatter.formatForSpeech(number, pairMode)

            val display = "$cleanPrefix $pairedDisplay"
            val spoken = "$spokenPrefix $pairedSpeech"

            return AnnouncementResult(
                displayText = display,
                spokenText = spoken,
                callerIdentifier = pairedDisplay,
                isKnownContact = false,
                hasMtnCarrier = false
            )
        }

        return unknownCaller(cleanPrefix, spokenPrefix)
    }

    /** Adds the calling app, e.g. "Incoming call from Jean, on WhatsApp". */
    fun withCallingApp(result: AnnouncementResult, appLabel: String, isVideo: Boolean): AnnouncementResult {
        val suffix = if (isVideo) ", $appLabel video call" else ", on $appLabel"
        return result.copy(
            displayText = result.displayText + suffix,
            spokenText = result.spokenText + sanitizeMtnForSpeech(suffix)
        )
    }

    private fun unknownCaller(cleanPrefix: String, spokenPrefix: String): AnnouncementResult {
        // Unknown / private number with no caller ID
        val unknownText = "unknown caller"
        return AnnouncementResult(
            displayText = "$cleanPrefix Unknown Caller",
            spokenText = "$spokenPrefix $unknownText",
            callerIdentifier = "Unknown Caller",
            isKnownContact = false,
            hasMtnCarrier = false
        )
    }
}
