package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Decides whether an unsaved number is likely spam: the user marked it in History,
 * or the phone already blocked a call from it (blocked calls are kept in the call log).
 */
object SpamChecker {

    private const val TAG = "SpamChecker"
    private const val PREFS_NAME = "spam_numbers"
    private const val KEY_NUMBERS = "numbers"
    private const val SIGNIFICANT_DIGITS = 9

    fun isLikelySpam(context: Context, number: String?): Boolean {
        val key = keyOf(number) ?: return false
        return key in markedNumbers(context) || wasBlockedBefore(context, key)
    }

    fun isMarked(context: Context, number: String?): Boolean {
        val key = keyOf(number) ?: return false
        return key in markedNumbers(context)
    }

    fun setMarked(context: Context, number: String?, marked: Boolean) {
        val key = keyOf(number) ?: return
        val updated = markedNumbers(context).toMutableSet().apply { if (marked) add(key) else remove(key) }
        DeviceStorage.prefs(context, PREFS_NAME).edit().putStringSet(KEY_NUMBERS, updated).apply()
    }

    fun markedNumbers(context: Context): Set<String> =
        DeviceStorage.prefs(context, PREFS_NAME).getStringSet(KEY_NUMBERS, emptySet()).orEmpty()

    /** The last 9 digits, which identify a line whether or not it is written with +250. */
    fun keyOf(number: String?): String? {
        val digits = number?.filter { it.isDigit() } ?: return null
        return if (digits.length < 6) null else digits.takeLast(SIGNIFICANT_DIGITS)
    }

    private fun wasBlockedBefore(context: Context, key: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        return try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NUMBER} LIKE ?",
                arrayOf(CallLog.Calls.BLOCKED_TYPE.toString(), "%$key"),
                null
            )?.use { it.count > 0 } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Cannot read blocked calls", e)
            false
        }
    }
}
