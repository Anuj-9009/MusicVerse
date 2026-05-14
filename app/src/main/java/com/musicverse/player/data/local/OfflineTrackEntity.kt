package com.musicverse.player.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_tracks")
data class OfflineTrackEntity(
    @PrimaryKey val id: String, // Spotify Track ID
    val title: String,
    val artist: String,
    val localFilePath: String,
    val downloadedAtMs: Long,
    val sizeBytes: Long
)
