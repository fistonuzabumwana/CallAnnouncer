package com.example.util

import android.content.Context

/** Setup facts that Android cannot report, so the app remembers them itself. */
object SetupState {

    private const val PREFS_NAME = "setup_state"
    private const val KEY_AUTO_START_CONFIRMED = "auto_start_confirmed"
    private const val KEY_LAST_BACKGROUND_ANNOUNCEMENT = "last_background_announcement"
    private const val KEY_PERMISSIONS_REQUESTED = "permissions_requested"

    /** True while an activity of this app is on screen. */
    @Volatile
    var isAppInForeground: Boolean = false

    fun isAutoStartConfirmed(context: Context): Boolean =
        DeviceStorage.prefs(context, PREFS_NAME).getBoolean(KEY_AUTO_START_CONFIRMED, false)

    fun setAutoStartConfirmed(context: Context, confirmed: Boolean) {
        DeviceStorage.prefs(context, PREFS_NAME).edit().putBoolean(KEY_AUTO_START_CONFIRMED, confirmed).apply()
    }

    /** When a real call was last announced while the app was closed; 0 if never. */
    fun lastBackgroundAnnouncementAt(context: Context): Long =
        DeviceStorage.prefs(context, PREFS_NAME).getLong(KEY_LAST_BACKGROUND_ANNOUNCEMENT, 0L)

    fun recordBackgroundAnnouncement(context: Context) {
        DeviceStorage.prefs(context, PREFS_NAME).edit()
            .putLong(KEY_LAST_BACKGROUND_ANNOUNCEMENT, System.currentTimeMillis())
            .apply()
    }

    /** Android only shows a permission dialog a limited number of times; afterwards Settings must be used. */
    fun werePermissionsRequested(context: Context): Boolean =
        DeviceStorage.prefs(context, PREFS_NAME).getBoolean(KEY_PERMISSIONS_REQUESTED, false)

    fun markPermissionsRequested(context: Context) {
        DeviceStorage.prefs(context, PREFS_NAME).edit().putBoolean(KEY_PERMISSIONS_REQUESTED, true).apply()
    }
}
