package com.musicverse.player.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Core domain models for the Hybrid Music Player.
 */

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String? = null,
    val durationMs: Long = 0,
    val isrc: String? = null,  // International Standard Recording Code — exact match key
    val spotifyUri: String? = null,
    val versions: List<TrackVersion> = emptyList()
)

@Serializable
data class TrackVersion(
    val id: String,
    val trackId: String,
    val type: TrackVersionType,
    val title: String,
    val artist: String,
    val youtubeVideoId: String? = null,
    val durationMs: Long = 0,
    val thumbnailUrl: String? = null,
    val aiVibeScore: Int? = null,          // 1-100 from Gemini "AI Vibe Check"
    val aiVibeReason: String? = null,      // One-sentence reason from Gemini
    val audioBadge: AudioBadge? = null,    // "Pristine Audio", "Crowd Singalong", etc.
    val sponsorBlockSegments: List<SkipSegment> = emptyList()
)

@Serializable
enum class TrackVersionType {
    @SerialName("studio") STUDIO,
    @SerialName("live") LIVE,
    @SerialName("acoustic") ACOUSTIC,
    @SerialName("cover") COVER,
    @SerialName("remix") REMIX
}

@Serializable
enum class AudioBadge(val label: String, val icon: String) {
    @SerialName("pristine") PRISTINE("Pristine Audio", "✦"),
    @SerialName("lossless") LOSSLESS("Lossless", "◈"),
    @SerialName("live") LIVE_RECORDING("Live Recording", "●"),
    @SerialName("crowd") CROWD_SINGALONG("Crowd Singalong", "♫"),
    @SerialName("acoustic") ACOUSTIC_SESSION("Acoustic Session", "♪"),
    @SerialName("remaster") REMASTERED("Remastered", "★"),
}

@Serializable
data class SkipSegment(
    val startTimeSec: Float,
    val endTimeSec: Float,
    val category: String   // "music_offtopic", "intro", "sponsor", etc.
)

/**
 * Playback state for the unified player queue.
 */
data class PlaybackState(
    val currentTrack: Track? = null,
    val currentVersion: TrackVersion? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val bufferedPositionMs: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = 0,
    val isShuffled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

enum class RepeatMode {
    OFF, ONE, ALL
}

/**
 * Spotify API response models.
 */
@Serializable
data class SpotifyTrackItem(
    val track: SpotifyTrack? = null
)

@Serializable
data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist> = emptyList(),
    val album: SpotifyAlbum? = null,
    @SerialName("duration_ms") val durationMs: Long = 0,
    @SerialName("external_ids") val externalIds: SpotifyExternalIds? = null
)

@Serializable
data class SpotifyArtist(
    val id: String,
    val name: String
)

@Serializable
data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage> = emptyList()
)

@Serializable
data class SpotifyImage(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)

@Serializable
data class SpotifyExternalIds(
    val isrc: String? = null
)

/**
 * MusicBrainz API response models.
 */
@Serializable
data class MusicBrainzRecordingResult(
    val recordings: List<MusicBrainzRecording> = emptyList()
)

@Serializable
data class MusicBrainzRecording(
    val id: String,
    val title: String,
    @SerialName("artist-credit") val artistCredit: List<MusicBrainzArtistCredit> = emptyList(),
    val length: Long? = null
)

@Serializable
data class MusicBrainzArtistCredit(
    val name: String,
    val artist: MusicBrainzArtist? = null
)

@Serializable
data class MusicBrainzArtist(
    val id: String,
    val name: String
)

/**
 * YouTube Data API response models.
 */
@Serializable
data class YouTubeSearchResult(
    val items: List<YouTubeItem> = emptyList()
)

@Serializable
data class YouTubeItem(
    val id: YouTubeItemId? = null,
    val snippet: YouTubeSnippet? = null
)

@Serializable
data class YouTubeItemId(
    val videoId: String? = null
)

@Serializable
data class YouTubeSnippet(
    val title: String = "",
    val description: String = "",
    val thumbnails: YouTubeThumbnails? = null,
    @SerialName("channelTitle") val channelTitle: String = ""
)

@Serializable
data class YouTubeThumbnails(
    val high: YouTubeThumbnail? = null,
    val medium: YouTubeThumbnail? = null,
    @SerialName("default") val default_: YouTubeThumbnail? = null
)

@Serializable
data class YouTubeThumbnail(
    val url: String = "",
    val width: Int? = null,
    val height: Int? = null
)

/**
 * YouTube video list response (for video details).
 */
@Serializable
data class YouTubeVideoListResult(
    val items: List<YouTubeVideoItem> = emptyList()
)

@Serializable
data class YouTubeVideoItem(
    val id: String = "",
    val snippet: YouTubeSnippet? = null,
    val contentDetails: YouTubeContentDetails? = null,
    val statistics: YouTubeStatistics? = null
)

@Serializable
data class YouTubeContentDetails(
    val duration: String = ""   // ISO 8601 duration, e.g. "PT4M33S"
)

@Serializable
data class YouTubeStatistics(
    val viewCount: String = "0",
    val likeCount: String = "0"
)

/**
 * YouTube comment models (for AI Vibe Check).
 */
@Serializable
data class YouTubeCommentThread(
    val items: List<YouTubeCommentItem> = emptyList()
)

@Serializable
data class YouTubeCommentItem(
    val snippet: YouTubeCommentSnippet? = null
)

@Serializable
data class YouTubeCommentSnippet(
    val topLevelComment: YouTubeTopLevelComment? = null
)

@Serializable
data class YouTubeTopLevelComment(
    val snippet: YouTubeCommentDetail? = null
)

@Serializable
data class YouTubeCommentDetail(
    val textDisplay: String = "",
    val likeCount: Int = 0
)

/**
 * SponsorBlock API response.
 */
@Serializable
data class SponsorBlockSegment(
    val segment: List<Float> = emptyList(),  // [startTime, endTime]
    val category: String = "",
    @SerialName("UUID") val uuid: String = ""
)
