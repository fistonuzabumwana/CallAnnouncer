package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService
import com.example.util.DebugLog

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        DebugLog.d("BootReceiver", "Boot broadcast received: $action")

        if (action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val settings = AppSettings.getInstance(context).getSettings()
            if (settings.isEnabled && settings.hasAcceptedPrivacyPolicy) {
                DebugLog.d("BootReceiver", "Call Announcer is enabled; starting service after $action")
                CallAnnouncerService.start(context)
            }
        }
    }
}
