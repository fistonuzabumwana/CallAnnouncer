package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CallLogEntity
import com.example.service.CallAnnouncerService
import com.example.tts.DirectTtsAnnouncer
import com.example.util.AnnouncementBuilder
import com.example.util.ContactLookupHelper
import com.example.util.PhoneCallNumberResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallStateReceiver : BroadcastReceiver() {

    private val tag = "CallStateReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val rawIncomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d(tag, "Broadcast received - State: $stateStr, RawNumber: $rawIncomingNumber")

        val stateInt = when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            else -> -1
        }

        val settings = AppSettings.getInstance(context).getSettings()

        when (stateInt) {
            TelephonyManager.CALL_STATE_OFFHOOK,
            TelephonyManager.CALL_STATE_IDLE -> {
                Log.d(tag, "Call ended or answered ($stateStr); stopping speech immediately")
                DirectTtsAnnouncer.stop()
                if (CallAnnouncerService.isServiceRunning) {
                    val serviceIntent = Intent(context, CallAnnouncerService::class.java).apply {
                        action = CallAnnouncerService.ACTION_INCOMING_CALL
                        putExtra(CallAnnouncerService.EXTRA_CALL_STATE, stateInt)
                    }
                    try {
                        context.startService(serviceIntent)
                    } catch (_: Exception) {}
                }
                return
            }

            TelephonyManager.CALL_STATE_RINGING -> {
                if (!settings.isEnabled) {
                    Log.d(tag, "Announcer disabled in settings; ignoring ring")
                    return
                }

                // If CallAnnouncerService is currently running in foreground, let the service handle it
                if (CallAnnouncerService.isServiceRunning) {
                    Log.d(tag, "Forwarding ringing event to active CallAnnouncerService")
                    val serviceIntent = Intent(context, CallAnnouncerService::class.java).apply {
                        action = CallAnnouncerService.ACTION_INCOMING_CALL
                        putExtra(CallAnnouncerService.EXTRA_CALL_STATE, stateInt)
                        putExtra(CallAnnouncerService.EXTRA_INCOMING_NUMBER, rawIncomingNumber)
                    }
                    try {
                        context.startService(serviceIntent)
                        return
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to deliver intent to running service, will announce directly", e)
                    }
                }

                // App is closed and service might not be running:
                // Use goAsync() to execute direct announcement reliably in background!
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Resolve number (with fallback to CallLog if extra is missing)
                        val incomingNumber = PhoneCallNumberResolver.resolveIncomingNumber(context, rawIncomingNumber)

                        // 2. Lookup contact name
                        val contactName = ContactLookupHelper.lookupContactName(context, incomingNumber)

                        // 3. Build announcement (applies MTN pronunciation exception & 2-number pairs)
                        val announcement = AnnouncementBuilder.buildAnnouncement(
                            contactName = contactName,
                            phoneNumber = incomingNumber,
                            prefix = settings.prefixText,
                            pairMode = settings.pairMode
                        )

                        Log.d(tag, "Direct announcement built: ${announcement.spokenText} (hasMtn: ${announcement.hasMtnCarrier})")

                        // 4. Check filters
                        if (announcement.isKnownContact && !settings.announceContacts) {
                            Log.d(tag, "Contact announcements disabled")
                            return@launch
                        }
                        if (!announcement.isKnownContact && !settings.announceUnknowns) {
                            Log.d(tag, "Unknown announcements disabled")
                            return@launch
                        }

                        // 5. Persist to history database
                        try {
                            val db = AppDatabase.getInstance(context)
                            db.callLogDao().insertAnnouncement(
                                CallLogEntity(
                                    rawPhoneNumber = incomingNumber ?: "Unknown",
                                    contactName = contactName,
                                    formattedDisplay = announcement.callerIdentifier,
                                    spokenAnnouncement = announcement.spokenText,
                                    isKnownContact = announcement.isKnownContact,
                                    timesAnnounced = settings.repeatCount
                                )
                            )
                        } catch (e: Exception) {
                            Log.w(tag, "Failed to persist call log", e)
                        }

                        // 6. Speak directly via DirectTtsAnnouncer
                        DirectTtsAnnouncer.speak(
                            context = context,
                            text = announcement.spokenText,
                            repeatCount = settings.repeatCount,
                            initialDelayMs = settings.initialDelayMs,
                            repeatDelayMs = settings.repeatDelayMs,
                            rate = settings.speechRate,
                            pitch = settings.speechPitch,
                            duckRingtone = settings.duckRingtone,
                            syncRingtoneVolume = settings.syncRingtoneVolume
                        )

                        // Also attempt to start the foreground service if allowed
                        try {
                            val serviceIntent = Intent(context, CallAnnouncerService::class.java).apply {
                                action = CallAnnouncerService.ACTION_START_SERVICE
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        } catch (_: Exception) {
                            // Ignored if background start is blocked by OS; DirectTtsAnnouncer is already speaking!
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error handling direct call announcement", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
