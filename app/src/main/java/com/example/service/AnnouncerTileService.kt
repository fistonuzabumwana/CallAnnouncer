package com.example.service

import com.example.i18n.tr

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/** Quick Settings tile that turns announcing on or off. */
@RequiresApi(Build.VERSION_CODES.N)
class AnnouncerTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        AnnouncerSwitch.toggle(applicationContext)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val on = AnnouncerSwitch.isOn(this)
        tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Call Announcer"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) tile.subtitle = if (on) tr("On") else tr("Paused")
        tile.updateTile()
    }
}
