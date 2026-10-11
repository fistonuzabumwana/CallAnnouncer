package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CallLogEntity
import com.example.tts.TtsManager
import com.example.util.AnnouncementBuilder
import com.example.util.ContactLookupHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CallAnnouncerService : Service() {

    private val tag = "CallAnnouncerService"
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private lateinit var ttsManager: TtsManager
    private lateinit var appSettings: AppSettings
    private lateinit var database: AppDatabase
    private var telephonyManager: TelephonyManager? = null

    private var telephonyCallback: TelephonyCallback? = null
    private var legacyListener: PhoneStateListener? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var lastIncomingNumber: String? = null
    private var isCurrentlyRinging = false

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service onCreate")
        appSettings = AppSettings.getInstance(this)
        database = AppDatabase.getInstance(this)
        ttsManager = TtsManager(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CallAnnouncer::WakeLock")

        createNotificationChannel()
        startForegroundServiceWithNotification("Monitoring incoming calls")

        registerTelephonyListeners()
    }

    private fun registerTelephonyListeners() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        handleCallState(state, null)
                    }
                }
                telephonyManager?.registerTelephonyCallback(mainExecutor, callback)
                telephonyCallback = callback
            } else {
                @Suppress("DEPRECATION")
                val listener = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        handleCallState(state, phoneNumber)
                    }
                }
                @Suppress("DEPRECATION")
                telephonyManager?.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
                legacyListener = listener
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Missing permission to listen to phone state in service", e)
        } catch (e: Exception) {
            Log.e(tag, "Failed to register telephony listener", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val incomingNumber = intent?.getStringExtra(EXTRA_INCOMING_NUMBER)
        val callState = intent?.getIntExtra(EXTRA_CALL_STATE, -1) ?: -1

        when (action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_ENABLED -> {
                val current = appSettings.getSettings()
                appSettings.updateSettings { it.copy(isEnabled = !current.isEnabled) }
                val statusText = if (!current.isEnabled) "Announcer Active" else "Announcer Paused"
                updateNotification(statusText)
            }
            ACTION_INCOMING_CALL -> {
                if (callState != -1) {
                    handleCallState(callState, incomingNumber)
                }
            }
        }

        return START_STICKY
    }

    fun handleCallState(state: Int, numberFromIntent: String?) {
        val number = numberFromIntent ?: lastIncomingNumber

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isCurrentlyRinging = true
                if (!numberFromIntent.isNullOrBlank()) {
                    lastIncomingNumber = numberFromIntent
                }
                triggerAnnouncement(number)
            }
            TelephonyManager.CALL_STATE_OFFHOOK,
            TelephonyManager.CALL_STATE_IDLE -> {
                isCurrentlyRinging = false
                lastIncomingNumber = null
                ttsManager.stop()
                releaseWakeLock()
                updateNotification("Monitoring incoming calls")
            }
        }
    }

    private fun triggerAnnouncement(rawNumber: String?) {
        val settings = appSettings.getSettings()
        if (!settings.isEnabled) {
            Log.d(tag, "Announcer is disabled in settings; skipping")
            return
        }

        acquireWakeLock()

        scope.launch {
            val contactName = ContactLookupHelper.lookupContactName(this@CallAnnouncerService, rawNumber)
            val announcement = AnnouncementBuilder.buildAnnouncement(
                contactName = contactName,
                phoneNumber = rawNumber,
                prefix = settings.prefixText,
                pairMode = settings.pairMode
            )

            // Check contact filters
            if (announcement.isKnownContact && !settings.announceContacts) {
                Log.d(tag, "Known contact announcements disabled")
                releaseWakeLock()
                return@launch
            }
            if (!announcement.isKnownContact && !settings.announceUnknowns) {
                Log.d(tag, "Unknown caller announcements disabled")
                releaseWakeLock()
                return@launch
            }

            // Save log entry to local database
            try {
                database.callLogDao().insertAnnouncement(
                    CallLogEntity(
                        rawPhoneNumber = rawNumber ?: "Unknown",
                        contactName = contactName,
                        formattedDisplay = announcement.callerIdentifier,
                        spokenAnnouncement = announcement.spokenText,
                        isKnownContact = announcement.isKnownContact,
                        timesAnnounced = settings.repeatCount
                    )
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to persist call announcement", e)
            }

            updateNotification("Announcing: ${announcement.callerIdentifier}")

            // Speak via TTS
            ttsManager.speakAnnouncement(
                text = announcement.spokenText,
                repeatCount = settings.repeatCount,
                initialDelayMs = settings.initialDelayMs,
                repeatDelayMs = settings.repeatDelayMs,
                rate = settings.speechRate,
                pitch = settings.speechPitch,
                onFinish = {
                    releaseWakeLock()
                    if (isCurrentlyRinging) {
                        updateNotification("Ringing: ${announcement.callerIdentifier}")
                    } else {
                        updateNotification("Monitoring incoming calls")
                    }
                }
            )
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(30000L) // Max 30 seconds
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to release wake lock", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Announcer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service notification to keep call announcer active"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, CallAnnouncerService::class.java).apply {
            action = ACTION_TOGGLE_ENABLED
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isEnabled = appSettings.getSettings().isEnabled
        val toggleActionText = if (isEnabled) "Pause" else "Resume"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Announcer")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, toggleActionText, togglePendingIntent)
            .build()
    }

    private fun startForegroundServiceWithNotification(statusText: String) {
        val notification = buildNotification(statusText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to startForeground with mediaPlayback type", e)
                try {
                    startForeground(NOTIFICATION_ID, notification)
                } catch (e2: Exception) {
                    Log.e(tag, "Failed to startForeground fallback", e2)
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Service onDestroy")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
                telephonyManager?.unregisterTelephonyCallback(telephonyCallback!!)
            } else if (legacyListener != null) {
                @Suppress("DEPRECATION")
                telephonyManager?.listen(legacyListener, PhoneStateListener.LISTEN_NONE)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to unregister telephony listener", e)
        }

        ttsManager.shutdown()
        releaseWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "call_announcer_foreground_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_TOGGLE_ENABLED = "com.example.action.TOGGLE_ENABLED"
        const val ACTION_INCOMING_CALL = "com.example.action.INCOMING_CALL"

        const val EXTRA_INCOMING_NUMBER = "extra_incoming_number"
        const val EXTRA_CALL_STATE = "extra_call_state"

        fun start(context: Context) {
            try {
                val intent = Intent(context, CallAnnouncerService::class.java).apply {
                    action = ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("CallAnnouncerService", "Failed to start service", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, CallAnnouncerService::class.java).apply {
                    action = ACTION_STOP_SERVICE
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("CallAnnouncerService", "Failed to stop service", e)
            }
        }
    }
}
