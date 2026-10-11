package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager

/**
 * Storage that stays readable after a reboot before the user unlocks the phone (Direct Boot),
 * so calls can be announced on the lock screen right after a restart.
 */
object DeviceStorage {

    fun context(context: Context): Context =
        context.applicationContext.createDeviceProtectedStorageContext()

    fun isUserUnlocked(context: Context): Boolean =
        context.getSystemService(UserManager::class.java)?.isUserUnlocked ?: true

    fun prefs(context: Context, name: String): SharedPreferences {
        val storage = context(context)
        // Credential-encrypted storage (where older versions saved prefs) is only readable once unlocked
        if (isUserUnlocked(context)) {
            storage.moveSharedPreferencesFrom(context.applicationContext, name)
        }
        return storage.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}
