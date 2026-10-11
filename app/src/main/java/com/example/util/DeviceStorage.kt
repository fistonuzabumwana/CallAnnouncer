package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import androidx.core.content.ContextCompat

/**
 * Storage that stays readable after a reboot before the user unlocks the phone (Direct Boot),
 * so calls can be announced on the lock screen right after a restart.
 */
object DeviceStorage {

    fun context(context: Context): Context =
        context.applicationContext.createDeviceProtectedStorageContext()

    fun isUserUnlocked(context: Context): Boolean =
        context.getSystemService(UserManager::class.java)?.isUserUnlocked ?: true

    /**
     * While the phone is locked, prefs not yet moved from credential storage (older versions)
     * read as empty. Pass [onMigrated] to re-read them once the phone is unlocked and moved.
     */
    fun prefs(
        context: Context,
        name: String,
        onMigrated: ((SharedPreferences) -> Unit)? = null
    ): SharedPreferences {
        val storage = context(context)
        if (isUserUnlocked(context)) {
            storage.moveSharedPreferencesFrom(context.applicationContext, name)
        } else if (onMigrated != null) {
            runWhenUnlocked(context) {
                storage.moveSharedPreferencesFrom(context.applicationContext, name)
                onMigrated(storage.getSharedPreferences(name, Context.MODE_PRIVATE))
            }
        }
        return storage.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    private fun runWhenUnlocked(context: Context, block: () -> Unit) {
        val appContext = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                appContext.unregisterReceiver(this)
                block()
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(Intent.ACTION_USER_UNLOCKED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        // The phone may have been unlocked between the check and the registration
        if (isUserUnlocked(appContext)) {
            try {
                appContext.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                return
            }
            // Posted so callers never run it while still constructing themselves
            Handler(Looper.getMainLooper()).post(block)
        }
    }
}
