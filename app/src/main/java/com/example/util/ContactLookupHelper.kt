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

    /**
     * The phonetic name saved in Contacts ("Phonetic first/middle/last name"), which tells
     * how the user wants the name pronounced. Null when none is saved.
     */
    fun lookupPhoneticName(context: Context, rawPhoneNumber: String?): String? {
        if (rawPhoneNumber.isNullOrBlank()) return null
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) return null

        return try {
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(rawPhoneNumber)
            )
            val contactId = context.contentResolver.query(
                lookupUri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null
            )?.use { if (it.moveToFirst()) it.getLong(0) else null } ?: return null

            val structuredName = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME
                ),
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId.toString(), structuredName),
                null
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                (0..2).mapNotNull { cursor.getString(it)?.trim()?.takeIf { part -> part.isNotEmpty() } }
                    .joinToString(" ")
                    .takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read phonetic name", e)
            null
        }
    }
}
