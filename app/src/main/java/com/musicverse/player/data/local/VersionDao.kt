package com.musicverse.player.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity — Discovered alternate version of a track.
 *
 * Links back to TrackEntity via trackId foreign key.
 * Stores YouTube metadata + AI vibe scoring from Gemini.
 */
@Entity(
    tableName = "versions",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class VersionEntity(
    @PrimaryKey val id: String,              // YouTube video ID
    val trackId: String,                      // FK → tracks.id
    val type: String,                         // "live", "acoustic", "cover", "remix"
    val title: String,
    val channelName: String = "",
    val youtubeVideoId: String,
    val thumbnailUrl: String? = null,
    val durationMs: Long = 0,
    val viewCount: Long = 0,
    val aiVibeScore: Int = 0,                 // 1-100 from Gemini
    val aiVibeReason: String = "",            // One-line explanation
    val audioBadge: String? = null,           // "pristine", "live", "acoustic", etc.
    val discoveredAt: Long = System.currentTimeMillis()
)

/**
 * Room DAO — Version persistence operations.
 */
@Dao
interface VersionDao {

    @Query("SELECT * FROM versions WHERE trackId = :trackId ORDER BY aiVibeScore DESC")
    fun getVersionsForTrack(trackId: String): Flow<List<VersionEntity>>

    @Query("SELECT COUNT(*) FROM versions WHERE trackId = :trackId")
    fun getVersionCountForTrack(trackId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM versions")
    fun getTotalVersionCount(): Flow<Int>

    @Query("SELECT * FROM versions ORDER BY aiVibeScore DESC LIMIT :limit")
    fun getTopVersions(limit: Int = 20): Flow<List<VersionEntity>>

    @Query("SELECT * FROM versions WHERE id = :versionId")
    suspend fun getVersionById(versionId: String): VersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<VersionEntity>)

    @Query("DELETE FROM versions WHERE trackId = :trackId")
    suspend fun deleteVersionsForTrack(trackId: String)

    @Query("DELETE FROM versions")
    suspend fun deleteAllVersions()

    @Query("SELECT DISTINCT trackId FROM versions")
    suspend fun getTrackIdsWithVersions(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM versions WHERE trackId = :trackId)")
    suspend fun hasVersionsForTrack(trackId: String): Boolean
}
