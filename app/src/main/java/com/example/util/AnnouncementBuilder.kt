package com.example.util

data class AnnouncementResult(
    val displayText: String,
    val spokenText: String,
    val callerIdentifier: String,
    val isKnownContact: Boolean
)

object AnnouncementBuilder {

    /**
     * Constructs the announcement payload.
     * If a contact name is found, announces the contact name (e.g. "Call from Alice").
     * If no contact name, announces the number grouped in pairs of two (e.g. "Call from 07, 85, 46").
     */
    fun buildAnnouncement(
        contactName: String?,
        phoneNumber: String?,
        prefix: String = "Incoming call from",
        pairMode: PairSpokenMode = PairSpokenMode.PAIRED_NUMBERS
    ): AnnouncementResult {
        val cleanPrefix = prefix.trim().ifEmpty { "Call from" }

        val hasValidContact = !contactName.isNullOrBlank()
        val hasValidNumber = !phoneNumber.isNullOrBlank()

        if (hasValidContact) {
            val name = contactName!!.trim()
            val spoken = "$cleanPrefix $name"
            val display = "$cleanPrefix $name"
            return AnnouncementResult(
                displayText = display,
                spokenText = spoken,
                callerIdentifier = name,
                isKnownContact = true
            )
        }

        if (hasValidNumber) {
            val number = phoneNumber!!.trim()
            val pairedDisplay = PhoneNumberFormatter.formatInPairs(number)
            val pairedSpeech = PhoneNumberFormatter.formatForSpeech(number, pairMode)

            val display = "$cleanPrefix $pairedDisplay"
            val spoken = "$cleanPrefix $pairedSpeech"

            return AnnouncementResult(
                displayText = display,
                spokenText = spoken,
                callerIdentifier = pairedDisplay,
                isKnownContact = false
            )
        }

        // Unknown / private number with no caller ID
        val unknownText = "unknown caller"
        return AnnouncementResult(
            displayText = "$cleanPrefix Unknown Caller",
            spokenText = "$cleanPrefix $unknownText",
            callerIdentifier = "Unknown Caller",
            isKnownContact = false
        )
    }
}
