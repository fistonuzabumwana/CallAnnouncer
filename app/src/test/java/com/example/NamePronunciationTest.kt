package com.example

import com.example.util.NamePronunciation
import com.example.util.NamePronunciation.Origin
import com.example.util.SpeechSegment
import org.junit.Assert.assertEquals
import org.junit.Test

class NamePronunciationTest {

    @Test
    fun recognisesRwandanNames() {
        listOf("Uwimana", "Nshimiyimana", "Mugisha", "Iradukunda", "Kwizera", "Ingabire", "Niyonzima",
            "Habimana", "Uwase", "Cyiza", "Mutoni", "Hakizimana", "Twagirayezu", "Nkurunziza").forEach {
            assertEquals(it, Origin.RWANDAN, NamePronunciation.originOf(it))
        }
    }

    @Test
    fun recognisesFrenchNames() {
        listOf("Jean", "Claude", "Théogène", "Jean-Claude", "Françoise", "Célestin").forEach {
            assertEquals(it, Origin.FRENCH, NamePronunciation.originOf(it))
        }
    }

    @Test
    fun leavesEnglishNamesAlone() {
        listOf("Alice", "Grace", "Maria", "Anita", "Amanda", "Patrick", "Eric", "Kevin", "Sarah", "MTN").forEach {
            assertEquals(it, Origin.OTHER, NamePronunciation.originOf(it))
        }
    }

    @Test
    fun respellsForEnglishVoices() {
        assertEquals("Oo-wee-mah-nah", NamePronunciation.respellForEnglish("Uwimana"))
        assertEquals("Ee-rah-doo-koon-dah", NamePronunciation.respellForEnglish("Iradukunda"))
        assertEquals("Moo-ghee-shah", NamePronunciation.respellForEnglish("Mugisha"))
        assertEquals("Chee-zah", NamePronunciation.respellForEnglish("Cyiza"))
        assertEquals("Nshee-mee-yee-mah-nah", NamePronunciation.respellForEnglish("Nshimiyimana"))
    }

    @Test
    fun speaksEachPartOfTheNameWithTheBestVoice() {
        val plan = NamePronunciation.plan(
            "Incoming call from Jean Claude Uwimana, on WhatsApp", "Jean Claude Uwimana", "en", setOf("en", "fr", "sw")
        )
        assertEquals(
            listOf(
                SpeechSegment("Incoming call from"),
                SpeechSegment("Jean Claude", "fr"),
                SpeechSegment("Uwimana", "sw"),
                SpeechSegment(", on WhatsApp")
            ),
            plan
        )
    }

    @Test
    fun respellsWhenNoSwahiliVoiceIsInstalled() {
        val plan = NamePronunciation.plan("Incoming call from Uwimana", "Uwimana", "en", setOf("en"))
        assertEquals(listOf(SpeechSegment("Incoming call from Oo-wee-mah-nah")), plan)
    }

    @Test
    fun englishNamesAndNumbersAreUnchanged() {
        assertEquals(listOf(SpeechSegment("Incoming call from Alice")),
            NamePronunciation.plan("Incoming call from Alice", "Alice", "en", setOf("en", "sw")))
        assertEquals(listOf(SpeechSegment("Incoming call from 07, 85")),
            NamePronunciation.plan("Incoming call from 07, 85", null, "en", setOf("sw")))
    }

    @Test
    fun nonEnglishMainVoiceWithoutSwahiliKeepsTheName() {
        assertEquals(listOf(SpeechSegment("Appel de Uwimana")),
            NamePronunciation.plan("Appel de Uwimana", "Uwimana", "fr", setOf("fr")))
    }
}
