package com.example.util

import android.Manifest
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.data.AppSettings
import com.example.service.CallAnnouncerService
import com.example.service.VoipCallListenerService
import com.example.tts.DirectTtsAnnouncer

enum class SetupItem {
    PHONE_PERMISSIONS,
    CONTACTS,
    CALLER_ID_ROLE,
    BATTERY_OPTIMIZATION,
    BACKGROUND_RESTRICTION,
    AUTO_START,
    VOICE,
    ANNOUNCER_RUNNING,
    NOTIFICATIONS,
    INTERNET_CALLS,
    UNUSED_APP_PAUSE,
    POWER_SAVER,
    RINGER_ON,
    REAL_CALL_TEST
}

enum class Importance { REQUIRED, RECOMMENDED }

data class ReadinessCheck(
    val item: SetupItem,
    val title: String,
    val description: String,
    val importance: Importance,
    val passed: Boolean,
    /** Label of the button that fixes it; null when only the user can change it. */
    val fixLabel: String? = null,
    /** Android cannot report this setting, so the user confirms it after changing it. */
    val needsConfirmation: Boolean = false
)

data class ReadinessReport(val checks: List<ReadinessCheck>) {
    val required: List<ReadinessCheck> get() = checks.filter { it.importance == Importance.REQUIRED }
    val recommended: List<ReadinessCheck> get() = checks.filter { it.importance == Importance.RECOMMENDED }
    val failingRequired: List<ReadinessCheck> get() = required.filter { !it.passed }

    /** Calls will be announced even while the app is closed. */
    val isReady: Boolean get() = failingRequired.isEmpty()

    /** Problems the app can open a fix for, required ones first. */
    val fixable: List<ReadinessCheck>
        get() = (failingRequired + recommended.filter { !it.passed }).filter { it.fixLabel != null }
}

/** Everything the checklist depends on, read from the phone in one place. */
data class DeviceFacts(
    val sdkInt: Int,
    val manufacturer: String,
    val hasPhoneState: Boolean,
    val hasCallLog: Boolean,
    val hasContacts: Boolean,
    val notificationsEnabled: Boolean,
    val callScreeningRoleAvailable: Boolean,
    val hasCallScreeningRole: Boolean,
    val ignoresBatteryOptimizations: Boolean,
    val isBackgroundRestricted: Boolean,
    val hasAutoStartScreen: Boolean,
    val autoStartConfirmed: Boolean,
    val ttsReady: Boolean,
    val hasOfflineVoice: Boolean,
    val announcerEnabled: Boolean,
    val serviceRunning: Boolean,
    val exemptFromUnusedAppPause: Boolean,
    val isPowerSaveMode: Boolean,
    val isRingerSilenced: Boolean,
    val lastBackgroundAnnouncementAt: Long,
    /** WhatsApp or other internet-call announcements are switched on. */
    val internetCallsWanted: Boolean = false,
    val hasNotificationAccess: Boolean = false
)

object ReadinessChecker {

    fun check(context: Context): ReadinessReport = evaluate(readFacts(context))

    fun readFacts(context: Context): DeviceFacts {
        val app = context.applicationContext
        fun granted(permission: String) =
            ContextCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED

        val powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            app.getSystemService(RoleManager::class.java)
        } else null
        val roleAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true

        return DeviceFacts(
            sdkInt = Build.VERSION.SDK_INT,
            manufacturer = AutoStartSettings.manufacturerName(),
            hasPhoneState = granted(Manifest.permission.READ_PHONE_STATE),
            hasCallLog = granted(Manifest.permission.READ_CALL_LOG),
            hasContacts = granted(Manifest.permission.READ_CONTACTS),
            notificationsEnabled = NotificationManagerCompat.from(app).areNotificationsEnabled(),
            callScreeningRoleAvailable = roleAvailable,
            hasCallScreeningRole = roleAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true,
            ignoresBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(app.packageName) != false,
            isBackgroundRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                activityManager?.isBackgroundRestricted == true,
            hasAutoStartScreen = AutoStartSettings.isAvailable(app),
            autoStartConfirmed = SetupState.isAutoStartConfirmed(app),
            ttsReady = DirectTtsAnnouncer.isReady.value,
            hasOfflineVoice = DirectTtsAnnouncer.availableLanguages.value.isNotEmpty(),
            announcerEnabled = AppSettings.getInstance(app).getSettings().isEnabled,
            serviceRunning = CallAnnouncerService.isServiceRunning,
            exemptFromUnusedAppPause = Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                app.packageManager.isAutoRevokeWhitelisted,
            isPowerSaveMode = powerManager?.isPowerSaveMode == true,
            isRingerSilenced = AnnouncementVolumeController.getInstance(app).isRingerSilenced(),
            lastBackgroundAnnouncementAt = SetupState.lastBackgroundAnnouncementAt(app),
            internetCallsWanted = AppSettings.getInstance(app).getSettings()
                .let { it.announceWhatsApp || it.announceOtherApps || it.announceMessages },
            hasNotificationAccess = VoipCallListenerService.isAccessGranted(app)
        )
    }

    fun evaluate(f: DeviceFacts): ReadinessReport {
        val backgroundVerified = f.lastBackgroundAnnouncementAt > 0
        val checks = buildList {
            add(
                ReadinessCheck(
                    item = SetupItem.PHONE_PERMISSIONS,
                    title = "Phone and call log access",
                    description = "Detects incoming calls and reads the caller's number.",
                    importance = Importance.REQUIRED,
                    passed = f.hasPhoneState && f.hasCallLog,
                    fixLabel = "Allow"
                )
            )
            add(
                ReadinessCheck(
                    item = SetupItem.CONTACTS,
                    title = "Contacts access",
                    description = "Speaks saved names instead of numbers, and lets Android wake the app for calls from contacts.",
                    importance = Importance.REQUIRED,
                    passed = f.hasContacts,
                    fixLabel = "Allow"
                )
            )
            if (f.callScreeningRoleAvailable) {
                add(
                    ReadinessCheck(
                        item = SetupItem.CALLER_ID_ROLE,
                        title = "Caller ID app",
                        description = "Android then wakes Call Announcer for every call, even when the phone has frozen or closed it. " +
                            "Calls are never blocked. Only one app can hold this: it replaces Truecaller or similar apps for spam blocking.",
                        importance = Importance.REQUIRED,
                        passed = f.hasCallScreeningRole,
                        fixLabel = "Set as caller ID app"
                    )
                )
            }
            add(
                ReadinessCheck(
                    item = SetupItem.BATTERY_OPTIMIZATION,
                    title = "Unrestricted battery use",
                    description = "Stops Android from putting the announcer to sleep to save battery.",
                    importance = Importance.REQUIRED,
                    passed = f.ignoresBatteryOptimizations,
                    fixLabel = "Allow"
                )
            )
            if (f.isBackgroundRestricted) {
                add(
                    ReadinessCheck(
                        item = SetupItem.BACKGROUND_RESTRICTION,
                        title = "Background activity allowed",
                        description = "Background use is restricted for this app. Open App info → Battery and choose \"Unrestricted\".",
                        importance = Importance.REQUIRED,
                        passed = false,
                        fixLabel = "Open app battery settings"
                    )
                )
            }
            if (f.hasAutoStartScreen) {
                add(
                    ReadinessCheck(
                        item = SetupItem.AUTO_START,
                        title = "${f.manufacturer} auto-start",
                        description = "${f.manufacturer} phones stop apps that are not on their auto-start list when they are closed. " +
                            "Turn Call Announcer on in that list, then confirm here.",
                        importance = Importance.REQUIRED,
                        passed = f.autoStartConfirmed || backgroundVerified,
                        fixLabel = "Open settings",
                        needsConfirmation = true
                    )
                )
            }
            add(
                ReadinessCheck(
                    item = SetupItem.VOICE,
                    title = "Offline voice installed",
                    description = when {
                        !f.ttsReady -> "No working text-to-speech engine. Install \"Speech Services by Google\"."
                        !f.hasOfflineVoice -> "The speech engine has no downloaded voice. Download one so calls are announced without internet."
                        else -> "A voice is installed and works without internet."
                    },
                    importance = Importance.REQUIRED,
                    passed = f.ttsReady && f.hasOfflineVoice,
                    fixLabel = if (!f.ttsReady) "Install speech engine" else "Download a voice"
                )
            )
            add(
                ReadinessCheck(
                    item = SetupItem.ANNOUNCER_RUNNING,
                    title = "Announcer switched on",
                    description = if (!f.announcerEnabled) "Announcements are paused." else "The background announcer is running.",
                    importance = Importance.REQUIRED,
                    passed = f.announcerEnabled && f.serviceRunning,
                    fixLabel = "Turn on"
                )
            )

            add(
                ReadinessCheck(
                    item = SetupItem.NOTIFICATIONS,
                    title = "Notifications",
                    description = "Shows the small \"Monitoring incoming calls\" notification that keeps the announcer visible to Android.",
                    importance = Importance.RECOMMENDED,
                    passed = f.notificationsEnabled,
                    fixLabel = "Allow"
                )
            )
            if (f.internetCallsWanted) {
                add(
                    ReadinessCheck(
                        item = SetupItem.INTERNET_CALLS,
                        title = "WhatsApp and app calls",
                        description = if (f.hasNotificationAccess) {
                            "Calls from WhatsApp, Telegram, Signal and similar apps are announced too."
                        } else {
                            "Allow notification access so calls from WhatsApp, Telegram, Signal and similar apps are announced. " +
                                "Only call notifications (and messages, if you turn that on) are read." +
                                if (f.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                                    " If Android says \"Restricted setting\", open App info → ⋮ → Allow restricted settings, then try again."
                                } else ""
                        },
                        importance = Importance.RECOMMENDED,
                        passed = f.hasNotificationAccess,
                        fixLabel = "Allow"
                    )
                )
            }
            if (f.sdkInt >= Build.VERSION_CODES.R) {
                add(
                    ReadinessCheck(
                        item = SetupItem.UNUSED_APP_PAUSE,
                        title = "Keep app active when unused",
                        description = "Turn off \"Pause app activity if unused\" so Android never removes the permissions after months without opening the app.",
                        importance = Importance.RECOMMENDED,
                        passed = f.exemptFromUnusedAppPause,
                        fixLabel = "Open settings"
                    )
                )
            }
            if (f.isPowerSaveMode) {
                add(
                    ReadinessCheck(
                        item = SetupItem.POWER_SAVER,
                        title = "Battery saver is on",
                        description = "Some phones stop background apps in battery saver. If calls are missed, turn it off or add Call Announcer to its exceptions.",
                        importance = Importance.RECOMMENDED,
                        passed = false,
                        fixLabel = "Open battery saver"
                    )
                )
            }
            add(
                ReadinessCheck(
                    item = SetupItem.RINGER_ON,
                    title = "Ringer on",
                    description = if (f.isRingerSilenced) "The phone is on silent or vibrate, so calls are not announced until the ringer is turned back on."
                    else "Calls are announced while the ringer is on.",
                    importance = Importance.RECOMMENDED,
                    passed = !f.isRingerSilenced
                )
            )
            add(
                ReadinessCheck(
                    item = SetupItem.REAL_CALL_TEST,
                    title = "Real call test",
                    description = if (backgroundVerified) "A real call was announced while the app was closed on this phone."
                    else "Close the app (swipe it away from recent apps), then ask someone to call you. This proves the phone lets Call Announcer work in the background.",
                    importance = Importance.RECOMMENDED,
                    passed = backgroundVerified
                )
            )
        }
        return ReadinessReport(checks)
    }
}
