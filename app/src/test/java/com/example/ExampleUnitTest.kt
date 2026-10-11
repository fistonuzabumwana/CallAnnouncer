package com.example

import com.example.util.AnnouncementBuilder
import com.example.util.PairSpokenMode
import com.example.util.PhoneNumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTwoNumbersAtATimeFormatting() {
        val formatted = PhoneNumberFormatter.formatInPairs("078546")
        assertEquals("07 85 46", formatted)
    }

    @Test
    fun testTwoNumbersAtATimeOddLength() {
        val formatted = PhoneNumberFormatter.formatInPairs("0785467")
        assertEquals("07 85 46 7", formatted)
    }

    @Test
    fun testTwoNumbersAtATimeWithPunctuation() {
        val formatted = PhoneNumberFormatter.formatInPairs("(07) 85-46")
        assertEquals("07 85 46", formatted)
    }

    @Test
    fun testSpeechFormattingInPairs() {
        val speech = PhoneNumberFormatter.formatForSpeech("078546", PairSpokenMode.PAIRED_NUMBERS)
        assertEquals("07, 85, 46", speech)
    }

    @Test
    fun testSpeechFormattingSpokenWords() {
        val speech = PhoneNumberFormatter.formatForSpeech("078546", PairSpokenMode.PAIRED_DIGIT_WORDS)
        assertEquals("zero seven, eight five, four six", speech)
    }

    @Test
    fun testAnnouncementWithContactName() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "Alice Smith",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.isKnownContact)
        assertEquals("Alice Smith", result.callerIdentifier)
        assertEquals("Incoming call from Alice Smith", result.spokenText)
    }

    @Test
    fun testAnnouncementWithoutContactNameReadsNumberInPairs() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = null,
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertFalse(result.isKnownContact)
        assertEquals("07 85 46", result.callerIdentifier)
        assertEquals("Incoming call from 07, 85, 46", result.spokenText)
    }

    @Test
    fun testAnnouncementWithBlankContactNameReadsNumberInPairs() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "   ",
            phoneNumber = "078546",
            prefix = "Call from"
        )
        assertFalse(result.isKnownContact)
        assertEquals("07 85 46", result.callerIdentifier)
        assertEquals("Call from 07, 85, 46", result.spokenText)
    }
}
