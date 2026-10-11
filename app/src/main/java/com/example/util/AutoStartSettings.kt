package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * Many manufacturers add their own "auto-start" switch on top of Android's battery settings.
 * While it is off, the app is force-stopped when swiped from recents and receives no call events.
 */
object AutoStartSettings {

    private val knownScreens = listOf(
        // TECNO / Infinix / itel (HiOS, XOS)
        ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
        // Xiaomi / Redmi / POCO
        ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        // Oppo / Realme / OnePlus
        ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
        ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
        ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        // Vivo
        ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
        // Huawei / Honor
        ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        // Asus
        ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")
    )

    private fun findIntent(context: Context): Intent? {
        val pm = context.packageManager
        return knownScreens
            .map { Intent().setComponent(it).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            .firstOrNull { intent ->
                @Suppress("DEPRECATION")
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
            }
    }

    fun isAvailable(context: Context): Boolean = findIntent(context) != null

    fun manufacturerName(): String = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }

    /** Opens the manufacturer's auto-start screen. Returns false if none could be opened. */
    fun open(context: Context): Boolean {
        val intent = findIntent(context) ?: return false
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
