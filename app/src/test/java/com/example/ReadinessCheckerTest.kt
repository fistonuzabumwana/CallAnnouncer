package com.example

import com.example.util.DeviceFacts
import com.example.util.ReadinessChecker
import com.example.util.SetupItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessCheckerTest {

    private val fullySetUp = DeviceFacts(
        sdkInt = 35,
        manufacturer = "TECNO",
        hasPhoneState = true,
        hasCallLog = true,
        hasContacts = true,
        notificationsEnabled = true,
        callScreeningRoleAvailable = true,
        hasCallScreeningRole = true,
        ignoresBatteryOptimizations = true,
        isBackgroundRestricted = false,
        hasAutoStartScreen = true,
        autoStartConfirmed = true,
        ttsReady = true,
        hasOfflineVoice = true,
        announcerEnabled = true,
        serviceRunning = true,
        exemptFromUnusedAppPause = true,
        isPowerSaveMode = false,
        isRingerSilenced = false,
        lastBackgroundAnnouncementAt = 0L
    )

    private fun failingRequired(facts: DeviceFacts) =
        ReadinessChecker.evaluate(facts).failingRequired.map { it.item }

    @Test
    fun fullySetUpPhoneIsReady() {
        assertTrue(ReadinessChecker.evaluate(fullySetUp).isReady)
    }

    @Test
    fun batteryOptimizationBlocksReadiness() {
        assertEquals(
            listOf(SetupItem.BATTERY_OPTIMIZATION),
            failingRequired(fullySetUp.copy(ignoresBatteryOptimizations = false))
        )
    }

    @Test
    fun missingCallerIdRoleBlocksReadiness() {
        assertEquals(listOf(SetupItem.CALLER_ID_ROLE), failingRequired(fullySetUp.copy(hasCallScreeningRole = false)))
    }

    @Test
    fun callerIdRoleIsSkippedWhenThePhoneHasNone() {
        val facts = fullySetUp.copy(callScreeningRoleAvailable = false, hasCallScreeningRole = false)
        assertTrue(ReadinessChecker.evaluate(facts).isReady)
    }

    @Test
    fun unconfirmedAutoStartBlocksReadiness() {
        assertEquals(listOf(SetupItem.AUTO_START), failingRequired(fullySetUp.copy(autoStartConfirmed = false)))
    }

    @Test
    fun realBackgroundCallProvesAutoStart() {
        val facts = fullySetUp.copy(autoStartConfirmed = false, lastBackgroundAnnouncementAt = 1L)
        assertTrue(ReadinessChecker.evaluate(facts).isReady)
    }

    @Test
    fun autoStartIsSkippedOnPhonesWithoutIt() {
        val facts = fullySetUp.copy(hasAutoStartScreen = false, autoStartConfirmed = false)
        assertTrue(ReadinessChecker.evaluate(facts).isReady)
    }

    @Test
    fun missingVoiceBlocksReadiness() {
        assertEquals(listOf(SetupItem.VOICE), failingRequired(fullySetUp.copy(hasOfflineVoice = false)))
    }

    @Test
    fun recommendedItemsDoNotBlockReadiness() {
        val facts = fullySetUp.copy(
            notificationsEnabled = false,
            isPowerSaveMode = true,
            isRingerSilenced = true,
            exemptFromUnusedAppPause = false
        )
        assertTrue(ReadinessChecker.evaluate(facts).isReady)
    }

    @Test
    fun fixListPutsRequiredItemsFirstAndSkipsUnfixable() {
        val facts = fullySetUp.copy(
            notificationsEnabled = false,
            ignoresBatteryOptimizations = false,
            isRingerSilenced = true
        )
        val fixable = ReadinessChecker.evaluate(facts).fixable.map { it.item }
        assertEquals(listOf(SetupItem.BATTERY_OPTIMIZATION, SetupItem.NOTIFICATIONS), fixable)
        assertFalse(SetupItem.RINGER_ON in fixable)
    }
}
