package com.example

import com.example.util.AnnouncementVolumeController.Companion.announcementVolumeFor
import com.example.util.AnnouncementVolumeController.Companion.duckedRingVolume
import org.junit.Assert.assertEquals
import org.junit.Test

class AnnouncementVolumeTest {

    @Test
    fun announcementIsAboutThirtyPercentAboveRingtone() {
        // Ringtone 8/16 (50%) -> announcement 80% of a 7-step alarm stream = ceil(5.6)
        assertEquals(6, announcementVolumeFor(ringVolume = 8, ringMax = 16, currentAnnounce = 0, announceMax = 7))
    }

    @Test
    fun announcementIsCappedAtMaximum() {
        assertEquals(15, announcementVolumeFor(ringVolume = 15, ringMax = 15, currentAnnounce = 3, announceMax = 15))
    }

    @Test
    fun announcementNeverLowersUsersAlarmVolume() {
        assertEquals(15, announcementVolumeFor(ringVolume = 2, ringMax = 15, currentAnnounce = 15, announceMax = 15))
    }

    @Test
    fun announcementIsAudibleEvenWithQuietRingtone() {
        // Ringtone 1/15 (~7%) -> ~37% of 15 = ceil(5.5)
        assertEquals(6, announcementVolumeFor(ringVolume = 1, ringMax = 15, currentAnnounce = 0, announceMax = 15))
    }

    @Test
    fun ringtoneIsDuckedToAQuarter() {
        assertEquals(4, duckedRingVolume(15))
        assertEquals(2, duckedRingVolume(8))
    }

    @Test
    fun duckedRingtoneNeverReachesZero() {
        assertEquals(1, duckedRingVolume(2))
        assertEquals(1, duckedRingVolume(1))
    }
}
