package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_announcements")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val rawPhoneNumber: String,
    val contactName: String?,
    val formattedDisplay: String,
    val spokenAnnouncement: String,
    val isKnownContact: Boolean,
    val timesAnnounced: Int = 1,
    /** Calling app for internet calls (e.g. "WhatsApp"); null for regular phone calls. */
    val source: String? = null
)
