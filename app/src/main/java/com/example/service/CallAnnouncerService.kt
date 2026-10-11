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
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import com.example.util.DebugLog
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppSettings
import com.example.tts.DirectTtsAnnouncer
import com.example.util.ReadinessChecker
import com.example.util.SetupItem

class CallAnnouncerService : Service() {

    private val tag = "CallAnnouncerService"

    private lateinit var appSettings: AppSettings
    private var telephonyManager: TelephonyManager? = null

    private var telephonyCallback: TelephonyCallback? = null
    private var legacyListener: PhoneStateListener? = null

    override fun onCreate() {
        super.onCreate()
        DebugLog.d(tag, "Service onCreate")
        isServiceRunning = true
        instance = this

        appSettings = AppSettings.getInstance(this)
        DirectTtsAnnouncer.initialize(this)

        createNotificationChannel()
        startForegroundServiceWithNotification(idleStatusText())

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
                isServiceRunning = false
                instance = null
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        DebugLog.d(tag, "App swiped from recents; foreground service continuing")
        // Keep foreground status notification active even when the app is closed
        if (appSettings.getSettings().isEnabled) {
            updateNotification(idleStatusText())
        }
    }

    fun handleCallState(state: Int, numberFromIntent: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                if (!appSettings.getSettings().isEnabled) {
                    DebugLog.d(tag, "Announcer is disabled in settings; skipping")
                    return
                }
                IncomingCallHandler.onRinging(this, numberFromIntent) { callerIdentifier ->
                    updateNotification("Announcing: $callerIdentifier")
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK,
            TelephonyManager.CALL_STATE_IDLE -> {
                IncomingCallHandler.onCallEnded(this)
                updateNotification(idleStatusText())
            }
        }
    }

    /** Warns from the notification when a phone setting (e.g. after an update) blocks announcements. */
    private fun idleStatusText(): String {
        val blocking = try {
            ReadinessChecker.check(this).failingRequired
                // The voice engine is still starting here, and this service is the one running
                .filter { it.item != SetupItem.VOICE && it.item != SetupItem.ANNOUNCER_RUNNING }
        } catch (e: Exception) {
            emptyList()
        }
        return if (blocking.isEmpty()) "Monitoring incoming calls"
        else "Setup incomplete: ${blocking.first().title}. Tap to fix."
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // mediaPlayback may not be started from BOOT_COMPLETED on Android 15+, specialUse can
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start foreground", e)
        }
    }

    fun showStatus(statusText: String) = updateNotification(statusText)

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLog.d(tag, "Service onDestroy")
        isServiceRunning = false
        instance = null

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

        DirectTtsAnnouncer.stop()
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

        @Volatile
        var isServiceRunning = false

        @Volatile
        var instance: CallAnnouncerService? = null

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
