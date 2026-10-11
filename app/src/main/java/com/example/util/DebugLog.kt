package com.example.util

import android.util.Log
import com.example.BuildConfig

/** Debug-level logging that is compiled into release builds but never printed there. */
object DebugLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }
}
