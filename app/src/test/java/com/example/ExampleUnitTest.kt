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
    fun testSpeechFormattingInPairs07PronouncedAsZeroSeven() {
        val speech = PhoneNumberFormatter.formatForSpeech("078546", PairSpokenMode.PAIRED_NUMBERS)
        // 07 is explicitly formatted as "zero seven" so TTS does not drop the leading zero
        assertEquals("zero seven, 85, 46", speech)
    }

    @Test
    fun testSpeechFormattingZerosInBetween() {
        // Number with 07 and another zero in between: 07 05 46
        val speech = PhoneNumberFormatter.formatForSpeech("070546", PairSpokenMode.PAIRED_NUMBERS)
        assertEquals("zero seven, zero five, 46", speech)

        // Number with double zero in between: 07 00 46
        val speechDoubleZero = PhoneNumberFormatter.formatForSpeech("070046", PairSpokenMode.PAIRED_NUMBERS)
        assertEquals("zero seven, zero zero, 46", speechDoubleZero)

        // Number with zero at the end: 07 85 03
        val speechTrailingZero = PhoneNumberFormatter.formatForSpeech("078503", PairSpokenMode.PAIRED_NUMBERS)
        assertEquals("zero seven, 85, zero three", speechTrailingZero)
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
        assertEquals("Incoming call from zero seven, 85, 46", result.spokenText)
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
        assertEquals("Call from zero seven, 85, 46", result.spokenText)
    }

    @Test
    fun testMtnPronunciationUppercase() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "Yves MTN",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.isKnownContact)
        assertTrue(result.hasMtnCarrier)
        assertEquals("Yves MTN", result.callerIdentifier)
        // Must be pronounced as "M T N", never "mountain"
        assertEquals("Incoming call from Yves M T N", result.spokenText)
    }

    @Test
    fun testMtnPronunciationLowercase() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "Yves mtn",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.hasMtnCarrier)
        assertEquals("Incoming call from Yves M T N", result.spokenText)
    }

    @Test
    fun testMtnPronunciationMixedCase() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "Yves Mtn",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.hasMtnCarrier)
        assertEquals("Incoming call from Yves M T N", result.spokenText)
    }

    @Test
    fun testMtnPronunciationPrefix() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "MTN Eve",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.hasMtnCarrier)
        assertEquals("Incoming call from M T N Eve", result.spokenText)
    }

    @Test
    fun testMtnPronunciationWithParentheses() {
        val result = AnnouncementBuilder.buildAnnouncement(
            contactName = "Eve (MTN)",
            phoneNumber = "078546",
            prefix = "Incoming call from"
        )
        assertTrue(result.hasMtnCarrier)
        assertEquals("Incoming call from Eve (M T N)", result.spokenText)
    }

    @Test
    fun testVolumeScaleProportions() {
        val maxVolume = 10
        val currentVolume90 = 9
        val percent90 = currentVolume90.toFloat() / maxVolume
        assertEquals(0.9f, percent90, 0.001f)

        val currentVolume50 = 5
        val percent50 = currentVolume50.toFloat() / maxVolume
        assertEquals(0.5f, percent50, 0.001f)

        val ducked90 = (currentVolume90 * 0.25f).toInt().coerceIn(1, currentVolume90)
        assertEquals(2, ducked90)
    }
}
