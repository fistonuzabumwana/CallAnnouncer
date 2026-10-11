package com.example

import android.app.Application
import android.util.Log
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService
import com.example.tts.DirectTtsAnnouncer

class CallAnnouncerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("CallAnnouncerApp", "Application onCreate")

        // Eagerly prepare TextToSpeech engine
        DirectTtsAnnouncer.initialize(this)

        // If announcer is enabled, start the background service
        val isEnabled = AppSettings.getInstance(this).getSettings().isEnabled
        if (isEnabled) {
            CallAnnouncerService.start(this)
        }
    }
}
