package com.example

import android.app.Application
import android.media.AudioManager
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementVolumeController
import com.example.util.DebugLog

class CallAnnouncerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugLog.d("CallAnnouncerApp", "Application onCreate")

        restoreVolumesLeftFromKilledProcess()

        // Eagerly prepare TextToSpeech engine
        DirectTtsAnnouncer.initialize(this)

        // If announcer is enabled, start the background service
        val isEnabled = AppSettings.getInstance(this).getSettings().isEnabled
        if (isEnabled) {
            CallAnnouncerService.start(this)
        }
    }

    private fun restoreVolumesLeftFromKilledProcess() {
        val controller = AnnouncementVolumeController.getInstance(this)
        if (!controller.hasPendingRestore()) return
        val audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        if (audioManager?.mode == AudioManager.MODE_NORMAL) {
            DebugLog.d("CallAnnouncerApp", "Restoring volumes left changed by a previous process")
            controller.restore()
        }
    }
}
