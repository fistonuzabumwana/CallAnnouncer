package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat

object PhoneCallNumberResolver {

    private const val TAG = "PhoneCallNumberResolver"

    /**
     * Resolves the incoming phone number. If the number from the broadcast intent is null or empty,
     * attempts to query the CallLog for the most recent incoming or ringing call.
     */
    fun resolveIncomingNumber(context: Context, numberFromIntent: String?): String? {
        if (!numberFromIntent.isNullOrBlank()) {
            return numberFromIntent
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.d(TAG, "READ_CALL_LOG not granted; cannot query fallback CallLog")
            return null
        }

        return try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                    val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                    if (dateIdx != -1 && numIdx != -1) {
                        val callDate = it.getLong(dateIdx)
                        // Only consider if the call log entry is very recent (within 20 seconds)
                        if (System.currentTimeMillis() - callDate < 20000L) {
                            val number = it.getString(numIdx)
                            if (!number.isNullOrBlank()) {
                                Log.d(TAG, "Resolved recent number from CallLog: $number")
                                return number
                            }
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query CallLog fallback", e)
            null
        }
    }
}
