package com.musicverse.player.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String,
    val durationMs: Long,
    val isrc: String? = null,
    val youtubeVideoId: String? = null,
    val isDownloaded: Boolean = false,
    val aiVibeScore: Int? = null,
    val addedAt: Long = System.currentTimeMillis()
)
