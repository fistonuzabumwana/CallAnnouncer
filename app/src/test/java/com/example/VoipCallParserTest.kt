package com.example

import com.example.util.AnnouncementBuilder
import com.example.util.CallNotification
import com.example.util.CallingApp
import com.example.util.VoipCallParser
import com.example.util.VoipCallState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoipCallParserTest {

    private fun notification(
        packageName: String = "com.whatsapp",
        category: String? = "call",
        callStyleType: Int = 0,
        fullScreen: Boolean = false,
        title: String? = "Jean",
        text: String? = "Incoming voice call",
        person: String? = null
    ) = CallNotification(packageName, category, callStyleType, fullScreen, false, person, title, text)

    private fun ringing(n: CallNotification) = VoipCallParser.parse(n) as VoipCallState.Ringing

    @Test
    fun whatsappVoiceCallIsRinging() {
        val call = ringing(notification(fullScreen = true)).call
        assertEquals(CallingApp.WHATSAPP, call.app)
        assertEquals("Jean", call.caller)
        assertFalse(call.isVideo)
        assertFalse(call.isUnsavedProfile)
    }

    @Test
    fun whatsappVideoCallInFrenchWithoutFullScreenIntent() {
        val call = ringing(notification(text = "Appel vidéo entrant")).call
        assertTrue(call.isVideo)
    }

    @Test
    fun unsavedWhatsappCallerIsMarked() {
        val call = ringing(notification(fullScreen = true, title = "~ Alice")).call
        assertEquals("Alice", call.caller)
        assertTrue(call.isUnsavedProfile)
    }

    @Test
    fun callStyleTypeWinsOverText() {
        assertTrue(VoipCallParser.parse(notification(callStyleType = 2, fullScreen = true)) is VoipCallState.NotRinging)
        val call = ringing(notification(callStyleType = 1, text = null, person = "Eric", title = "Telegram",
            packageName = "org.telegram.messenger")).call
        assertEquals(CallingApp.TELEGRAM, call.app)
        assertEquals("Eric", call.caller)
    }

    @Test
    fun ongoingAndMissedCallsAreNotRinging() {
        assertTrue(VoipCallParser.parse(notification(text = "Ongoing voice call")) is VoipCallState.NotRinging)
        assertTrue(VoipCallParser.parse(notification(text = "Missed voice call")) is VoipCallState.NotRinging)
        assertTrue(VoipCallParser.parse(notification(text = "Calling…")) is VoipCallState.NotRinging)
    }

    @Test
    fun messagesAndUnknownAppsAreIgnored() {
        assertTrue(VoipCallParser.parse(notification(category = "msg", text = "Hello")) is VoipCallState.Unrelated)
        assertTrue(VoipCallParser.parse(notification(packageName = "com.example.random")) is VoipCallState.Unrelated)
    }

    @Test
    fun appNameAsTitleGivesNoCaller() {
        val call = ringing(notification(packageName = "org.thoughtcrime.securesms", title = "Signal", fullScreen = true)).call
        assertNull(call.caller)
    }

    @Test
    fun recognisesPhoneNumbers() {
        assertTrue(VoipCallParser.looksLikePhoneNumber("+250 788 123 456"))
        assertFalse(VoipCallParser.looksLikePhoneNumber("Jean Paul"))
    }

    @Test
    fun announcementMentionsTheApp() {
        val base = AnnouncementBuilder.buildAnnouncement("Jean", null, "Incoming call from")
        assertEquals("Incoming call from Jean, on WhatsApp",
            AnnouncementBuilder.withCallingApp(base, "WhatsApp", isVideo = false).spokenText)
        assertEquals("Incoming call from Jean, WhatsApp video call",
            AnnouncementBuilder.withCallingApp(base, "WhatsApp", isVideo = true).spokenText)
    }
}
