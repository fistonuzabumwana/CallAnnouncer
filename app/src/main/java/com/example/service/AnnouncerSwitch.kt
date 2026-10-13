package com.example.service

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.TileService
import com.example.data.AppSettings
import com.example.receiver.AnnouncerWidgetProvider
import com.example.tts.DirectTtsAnnouncer

/** Turns announcing on or off from anywhere (app, notification, Quick Settings tile, widget). */
object AnnouncerSwitch {

    fun isOn(context: Context): Boolean = AppSettings.getInstance(context).getSettings().isEnabled

    fun toggle(context: Context) = setEnabled(context, !isOn(context))

    fun setEnabled(context: Context, enabled: Boolean) {
        val settings = AppSettings.getInstance(context)
        settings.updateSettings { it.copy(isEnabled = enabled) }
        if (enabled) {
            if (settings.getSettings().hasAcceptedPrivacyPolicy) CallAnnouncerService.start(context)
        } else {
            CallAnnouncerService.stop(context)
            DirectTtsAnnouncer.stop()
        }
        refreshControls(context)
    }

    /** Redraws the tile and widgets after the on/off state changed. */
    fun refreshControls(context: Context) {
        AnnouncerWidgetProvider.updateAll(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                TileService.requestListeningState(context, ComponentName(context, AnnouncerTileService::class.java))
            } catch (_: Exception) {
            }
        }
    }
}
