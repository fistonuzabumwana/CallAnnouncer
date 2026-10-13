package com.example.receiver

import com.example.i18n.tr

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import com.example.service.AnnouncerSwitch

/** Home-screen widget showing whether calls are announced; tapping it turns announcing on or off. */
class AnnouncerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { manager.updateAppWidget(it, views(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE) {
            AnnouncerSwitch.toggle(context)
            return
        }
        super.onReceive(context, intent)
    }

    companion object {
        private const val ACTION_TOGGLE = "com.example.action.WIDGET_TOGGLE"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, AnnouncerWidgetProvider::class.java))
            if (ids.isNotEmpty()) manager.updateAppWidget(ids, views(context))
        }

        private fun views(context: Context): RemoteViews {
            val on = AnnouncerSwitch.isOn(context)
            val toggle = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, AnnouncerWidgetProvider::class.java).setAction(ACTION_TOGGLE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return RemoteViews(context.packageName, R.layout.widget_announcer).apply {
                setTextViewText(R.id.widget_status, if (on) tr("Announcing calls") else tr("Paused · tap to turn on"))
                setImageViewResource(R.id.widget_icon, if (on) R.drawable.ic_announcer_on else R.drawable.ic_announcer_off)
                setInt(R.id.widget_root, "setBackgroundResource",
                    if (on) R.drawable.widget_background_on else R.drawable.widget_background_off)
                setOnClickPendingIntent(R.id.widget_root, toggle)
            }
        }
    }
}
