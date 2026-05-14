package com.musicverse.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineTrackDao {
    @Query("SELECT * FROM offline_tracks")
    fun getAllOfflineTracks(): Flow<List<OfflineTrackEntity>>

    @Query("SELECT * FROM offline_tracks WHERE id = :id")
    suspend fun getOfflineTrackById(id: String): OfflineTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineTrack(track: OfflineTrackEntity)

    @Query("DELETE FROM offline_tracks WHERE id = :id")
    suspend fun deleteOfflineTrack(id: String)
}
