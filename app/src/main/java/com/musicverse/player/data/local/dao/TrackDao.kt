package com.musicverse.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.musicverse.player.data.local.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY addedAt DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)
}
