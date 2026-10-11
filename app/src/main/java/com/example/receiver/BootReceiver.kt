package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d("BootReceiver", "Boot broadcast received: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val isEnabled = AppSettings.getInstance(context).getSettings().isEnabled
            if (isEnabled) {
                Log.d("BootReceiver", "Call Announcer is enabled; starting service on device boot")
                CallAnnouncerService.start(context)
            }
        }
    }
}
