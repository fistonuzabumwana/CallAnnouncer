package com.example

import com.example.util.AnnouncementBuilder
import com.example.util.AnnouncementLanguage
import com.example.util.CallerNetwork
import com.example.util.MessageNotification
import com.example.util.MessageParser
import com.example.util.MessagingApp
import com.example.util.NamePronunciation
import com.example.util.QuietHours
import com.example.util.SpeechSegment
import com.example.util.SpokenPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementFeaturesTest {

    @Test
    fun networkIsReadFromRwandanPrefixes() {
        assertEquals("MTN", CallerNetwork.of("0788123456"))
        assertEquals("MTN", CallerNetwork.of("+250 79 123 4567"))
        assertEquals("Airtel", CallerNetwork.of("0722936482"))
        assertEquals("Airtel", CallerNetwork.of("250733123456"))
        assertNull(CallerNetwork.of("078546"))
        assertNull(CallerNetwork.of("+254712345678"))
        assertNull(CallerNetwork.of(null))
    }

    @Test
    fun unknownNumberMentionsItsNetwork() {
        val result = AnnouncementBuilder.buildAnnouncement(null, "0788123456", sayNetwork = true)
        assertEquals("Incoming call from an M T N number, zero seven, 88, 12, 34, 56", result.spokenText)
        assertEquals("Incoming call from 07 88 12 34 56 (MTN)", result.displayText)
    }

    @Test
    fun networkIsNotSaidForContactsOrWhenOff() {
        assertEquals(
            "Incoming call from Jean",
            AnnouncementBuilder.buildAnnouncement("Jean", "0788123456", sayNetwork = true).spokenText
        )
        assertEquals(
            "Incoming call from zero seven, 22, 93, 64, 82",
            AnnouncementBuilder.buildAnnouncement(null, "0722936482", sayNetwork = false).spokenText
        )
    }

    @Test
    fun spamWarningComesFirst() {
        val result = AnnouncementBuilder.buildAnnouncement(null, "0722936482", possibleSpam = true, sayNetwork = true)
        assertEquals("Possible spam. Incoming call from an Airtel number, zero seven, 22, 93, 64, 82", result.spokenText)
    }

    @Test
    fun kinyarwandaAndFrenchWording() {
        val rw = AnnouncementBuilder.buildAnnouncement(
            null, null, prefix = AnnouncementLanguage.KINYARWANDA.defaultPrefix,
            language = AnnouncementLanguage.KINYARWANDA
        )
        assertEquals("Uguhamagara ni nimero itazwi", rw.spokenText)

        val fr = AnnouncementBuilder.buildAnnouncement(
            "Jean", null, prefix = "Appel de", language = AnnouncementLanguage.FRENCH,
            callingApp = "WhatsApp", isVideo = true
        )
        assertEquals("Appel de Jean, appel vidéo WhatsApp", fr.spokenText)
    }

    @Test
    fun spelledNameIsReadAsWritten() {
        val result = AnnouncementBuilder.buildAnnouncement("Cyusa", "0788123456", spokenName = "Choo-sah")
        assertEquals("Incoming call from Choo-sah", result.spokenText)
        assertEquals("Cyusa", result.callerIdentifier)
        assertEquals(SpokenPart.Kind.SPELLED, result.parts.last().kind)
    }

    @Test
    fun kinyarwandaPhrasesUseSwahiliVoiceAndNumbersStayInMainVoice() {
        val result = AnnouncementBuilder.buildAnnouncement(
            null, "0788123456", prefix = "Uguhamagara ni",
            language = AnnouncementLanguage.KINYARWANDA, sayNetwork = true
        )
        val plan = NamePronunciation.planParts(result.parts, "en", "rw", setOf("en", "sw"))
        assertEquals(
            listOf(
                SpeechSegment("Uguhamagara ni nimero ya M T N,", "sw"),
                SpeechSegment("zero seven, 88, 12, 34, 56", null)
            ),
            plan
        )
    }

    @Test
    fun frenchPhrasesUseFrenchVoiceWhenInstalled() {
        val result = AnnouncementBuilder.buildAnnouncement(
            "Uwimana", null, prefix = "Appel de", language = AnnouncementLanguage.FRENCH
        )
        val plan = NamePronunciation.planParts(result.parts, "en", "fr", setOf("en", "fr", "sw"))
        assertEquals(listOf(SpeechSegment("Appel de", "fr"), SpeechSegment("Uwimana", "sw")), plan)
    }

    @Test
    fun englishAnnouncementWithEnglishVoiceStaysOneSegment() {
        val result = AnnouncementBuilder.buildAnnouncement("Alice Smith", null, callingApp = "WhatsApp")
        val plan = NamePronunciation.planParts(result.parts, "en", "en", setOf("en"))
        assertEquals(listOf(SpeechSegment("Incoming call from Alice Smith, on WhatsApp", null)), plan)
    }

    @Test
    fun quietHoursCrossMidnight() {
        val start = 22 * 60
        val end = 7 * 60
        assertTrue(QuietHours.isQuiet(23 * 60, start, end))
        assertTrue(QuietHours.isQuiet(3 * 60, start, end))
        assertFalse(QuietHours.isQuiet(7 * 60, start, end))
        assertFalse(QuietHours.isQuiet(12 * 60, start, end))
        assertTrue(QuietHours.isQuiet(13 * 60, 12 * 60, 14 * 60))
        assertFalse(QuietHours.isQuiet(10 * 60, 10 * 60, 10 * 60))
    }

    @Test
    fun messageAnnouncement() {
        val plain = AnnouncementBuilder.buildMessageAnnouncement("Jean", "WhatsApp", AnnouncementLanguage.ENGLISH)
        assertEquals("Message from Jean, on WhatsApp", plain.spokenText)
        val withText = AnnouncementBuilder.buildMessageAnnouncement(
            "Jean", "WhatsApp", AnnouncementLanguage.ENGLISH, group = "Family", messageText = "See you at 5"
        )
        assertEquals("Message from Jean in Family, on WhatsApp. See you at 5", withText.spokenText)
    }

    @Test
    fun missedCallsSummary() {
        val result = AnnouncementBuilder.buildMissedCallsSummary(listOf("Jean", "Paul", "07 88 12"), AnnouncementLanguage.ENGLISH)
        assertEquals("You missed calls from Jean, Paul and zero seven, 88, 12", result.spokenText)
        assertEquals(
            "You missed a call from Jean",
            AnnouncementBuilder.buildMissedCallsSummary(listOf("Jean"), AnnouncementLanguage.ENGLISH).spokenText
        )
    }

    @Test
    fun parsesWhatsAppMessages() {
        val message = MessageParser.parse(
            MessageNotification(
                packageName = "com.whatsapp", category = "msg", isGroupSummary = false, isOngoing = false,
                title = "Family", text = "Jean: hi", conversationTitle = "Family (3 messages)",
                isGroupConversation = true, lastSender = "~ Jean", lastText = "I'll be calling later"
            )
        )!!
        assertEquals(MessagingApp.WHATSAPP, message.app)
        assertEquals("Jean", message.sender)
        assertEquals("Family", message.group)
        assertEquals("I'll be calling later", message.text)
    }

    @Test
    fun ignoresSummariesAndStatusNotifications() {
        fun parse(title: String?, text: String?, summary: Boolean = false, category: String? = "msg") =
            MessageParser.parse(
                MessageNotification("com.whatsapp", category, summary, false, title, text)
            )
        assertNull(parse("WhatsApp", "5 new messages", summary = true))
        assertNull(parse("WhatsApp", "Checking for new messages"))
        assertNull(parse("Jean", "Missed voice call"))
        assertNull(parse("Jean", "Incoming voice call", category = "call"))
        assertEquals("Jean", parse("Jean", "Hello")?.sender)
    }

    @Test
    fun smsFromDefaultSmsApp() {
        val message = MessageParser.parse(
            MessageNotification("com.example.sms", "msg", false, false, "MTN", "Your balance is 500"),
            smsPackage = "com.example.sms"
        )
        assertEquals(MessagingApp.SMS, message?.app)
        assertNull(
            MessageParser.parse(MessageNotification("com.random.app", "msg", false, false, "Jean", "Hello"))
        )
    }
}
