package com.example

import com.example.util.PhoneNumberMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberMatcherTest {

    @Test
    fun internationalAndLocalFormatsMatch() {
        assertTrue(PhoneNumberMatcher.isSameNumber("+250722936482", "0722936482"))
    }

    @Test
    fun punctuationIsIgnored() {
        assertTrue(PhoneNumberMatcher.isSameNumber("+250 (722) 936-482", "0722936482"))
    }

    @Test
    fun differentNumbersDoNotMatch() {
        assertFalse(PhoneNumberMatcher.isSameNumber("0722936482", "0722936483"))
    }

    @Test
    fun missingNumberNeverMatches() {
        assertFalse(PhoneNumberMatcher.isSameNumber(null, "0722936482"))
        assertFalse(PhoneNumberMatcher.isSameNumber("0722936482", null))
    }
}
