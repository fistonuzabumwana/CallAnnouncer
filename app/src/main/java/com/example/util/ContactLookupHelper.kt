package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

object ContactLookupHelper {

    private const val TAG = "ContactLookupHelper"

    /**
     * Looks up the contact's display name from the Android Contacts Provider.
     * Returns null if not found, if permission is missing, or if lookup fails.
     */
    fun lookupContactName(context: Context, rawPhoneNumber: String?): String? {
        if (rawPhoneNumber.isNullOrBlank()) return null

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            DebugLog.d(TAG, "READ_CONTACTS permission not granted")
            return null
        }

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(rawPhoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        val name = cursor.getString(columnIndex)
                        if (!name.isNullOrBlank()) {
                            return@use name
                        }
                    }
                }
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to lookup contact for number: $rawPhoneNumber", e)
            null
        }
    }
}
