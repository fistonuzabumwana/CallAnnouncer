package com.example.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.util.DebugLog
import com.example.data.AppSettings

/**
 * When the app holds the call-screening role, Telecom binds this service for every incoming call
 * before the phone starts ringing. The bind wakes the process even on OEM systems that freeze
 * background apps and hold back the PHONE_STATE broadcast (e.g. TECNO/Infinix "Hiber").
 *
 * Calls are never blocked; the service only uses the event to start the announcement.
 */
class CallAnnouncerScreeningService : CallScreeningService() {

    private val tag = "CallScreening"

    override fun onScreenCall(callDetails: Call.Details) {
        // Must answer quickly: the phone does not ring until Telecom gets a response
        try {
            respondToCall(callDetails, CallResponse.Builder().build())
        } catch (e: Exception) {
            Log.w(tag, "Failed to respond to call", e)
        }

        val isIncoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        if (!isIncoming) return

        if (!AppSettings.getInstance(this).getSettings().isEnabled) {
            DebugLog.d(tag, "Announcer disabled in settings; ignoring call")
            return
        }

        val number = callDetails.handle?.schemeSpecificPart
        DebugLog.d(tag, "Incoming call screened: $number")

        CallAnnouncerService.start(this)
        IncomingCallHandler.onRinging(this, number) { callerIdentifier ->
            CallAnnouncerService.instance?.showStatus("Announcing: $callerIdentifier")
        }
    }
}
