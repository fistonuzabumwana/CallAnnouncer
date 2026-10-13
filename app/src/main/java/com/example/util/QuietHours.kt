package com.example.util

import java.util.Calendar

object QuietHours {

    /** True when [minuteOfDay] falls between [start] and [end]; the range may cross midnight. */
    fun isQuiet(minuteOfDay: Int, start: Int, end: Int): Boolean = when {
        start == end -> false
        start < end -> minuteOfDay in start until end
        else -> minuteOfDay >= start || minuteOfDay < end
    }

    fun isQuietNow(start: Int, end: Int): Boolean {
        val now = Calendar.getInstance()
        return isQuiet(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE), start, end)
    }

    fun format(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
}
