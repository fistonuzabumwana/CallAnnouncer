package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService

class CallStateReceiver : BroadcastReceiver() {

    private val tag = "CallStateReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d(tag, "Phone state changed: $stateStr, number: $incomingNumber")

        val stateInt = when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            else -> -1
        }

        val settings = AppSettings.getInstance(context).getSettings()
        if (!settings.isEnabled && stateInt == TelephonyManager.CALL_STATE_RINGING) {
            Log.d(tag, "Announcer disabled in settings; ignoring incoming ring")
            return
        }

        val serviceIntent = Intent(context, CallAnnouncerService::class.java).apply {
            action = CallAnnouncerService.ACTION_INCOMING_CALL
            putExtra(CallAnnouncerService.EXTRA_CALL_STATE, stateInt)
            putExtra(CallAnnouncerService.EXTRA_INCOMING_NUMBER, incomingNumber)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start CallAnnouncerService from receiver", e)
        }
    }
}
