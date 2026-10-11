package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_announcements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAnnouncements(limit: Int): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(callLog: CallLogEntity): Long

    @Delete
    suspend fun deleteAnnouncement(callLog: CallLogEntity)

    @Query("DELETE FROM call_announcements")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM call_announcements")
    fun getAnnouncementCount(): Flow<Int>
}
