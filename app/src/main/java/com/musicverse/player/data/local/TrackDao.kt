package com.musicverse.player.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity — Cached track from Spotify import.
 * Uses ISRC as the primary key for exact matching across services.
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,          // Spotify track ID
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String? = null,
    val durationMs: Long = 0,
    val isrc: String? = null,
    val spotifyUri: String? = null,
    val importedAt: Long = System.currentTimeMillis()
)

/**
 * Room DAO — Track persistence operations.
 */
@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY importedAt DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE isrc = :isrc LIMIT 1")
    suspend fun getTrackByIsrc(isrc: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :trackId)")
    suspend fun trackExists(trackId: String): Boolean
}
