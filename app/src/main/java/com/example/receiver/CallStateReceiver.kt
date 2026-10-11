package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.example.util.DebugLog
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService
import com.example.service.IncomingCallHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * PHONE_STATE is one of the few implicit broadcasts still delivered to manifest receivers,
 * so this runs even when the app is closed and removed from recents.
 */
class CallStateReceiver : BroadcastReceiver() {

    private val tag = "CallStateReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        @Suppress("DEPRECATION")
        val rawIncomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        DebugLog.d(tag, "Broadcast received - State: $stateStr, RawNumber: $rawIncomingNumber")

        when (stateStr) {
            TelephonyManager.EXTRA_STATE_OFFHOOK,
            TelephonyManager.EXTRA_STATE_IDLE -> {
                DebugLog.d(tag, "Call answered or ended ($stateStr); stopping speech and restoring volumes")
                IncomingCallHandler.onCallEnded(context)
                if (CallAnnouncerService.isServiceRunning) {
                    sendToService(context, callState(stateStr), null, foreground = false)
                }
            }

            TelephonyManager.EXTRA_STATE_RINGING -> {
                if (!AppSettings.getInstance(context).getSettings().isEnabled) {
                    DebugLog.d(tag, "Announcer disabled in settings; ignoring ring")
                    return
                }

                val ringing = TelephonyManager.CALL_STATE_RINGING
                // The foreground service keeps the process alive for the whole announcement
                if (sendToService(context, ringing, rawIncomingNumber, foreground = !CallAnnouncerService.isServiceRunning)) {
                    return
                }

                // Android refused to start the service from the background (battery optimization
                // is on), so announce from here and keep the receiver alive while speaking.
                DebugLog.d(tag, "Announcing directly from receiver")
                val pendingResult = goAsync()
                val job = IncomingCallHandler.onRinging(context, rawIncomingNumber)
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        withTimeoutOrNull(MAX_RECEIVER_HOLD_MS) { job?.join() }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun sendToService(context: Context, state: Int, number: String?, foreground: Boolean): Boolean {
        val serviceIntent = Intent(context, CallAnnouncerService::class.java).apply {
            action = CallAnnouncerService.ACTION_INCOMING_CALL
            putExtra(CallAnnouncerService.EXTRA_CALL_STATE, state)
            putExtra(CallAnnouncerService.EXTRA_INCOMING_NUMBER, number)
        }
        return try {
            if (foreground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            true
        } catch (e: Exception) {
            Log.w(tag, "Could not deliver call state to service", e)
            false
        }
    }

    private fun callState(stateStr: String): Int = when (stateStr) {
        TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
        TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
        else -> TelephonyManager.CALL_STATE_IDLE
    }

    companion object {
        /** Stays under the 10s timeout Android applies to foreground broadcasts. */
        private const val MAX_RECEIVER_HOLD_MS = 9_000L
    }
}
