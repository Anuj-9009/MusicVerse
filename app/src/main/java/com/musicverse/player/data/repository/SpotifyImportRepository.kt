package com.musicverse.player.data.repository

import com.musicverse.player.data.api.SpotifyApiService
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.util.SpotifyAuthManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for importing and persisting tracks from Spotify.
 *
 * Handles:
 *   - Paginated fetching of Liked Songs
 *   - Full playlist iteration with track extraction
 *   - ISRC code extraction for precise version matching
 *   - Progress reporting via callback
 */
@Singleton
class SpotifyImportRepository @Inject constructor(
    private val spotifyApi: SpotifyApiService,
    private val trackDao: TrackDao,
    private val authManager: SpotifyAuthManager
) {
    /**
     * Observe all imported tracks.
     */
    fun getAllTracks(): Flow<List<TrackEntity>> = trackDao.getAllTracks()

    /**
     * Observe total imported track count.
     */
    fun getTrackCount(): Flow<Int> = trackDao.getTrackCount()

    /**
     * Import all liked songs from Spotify, paginated.
     *
     * @param onProgress Called with (imported, total) after each page
     * @return Total number of tracks imported
     */
    suspend fun importLikedSongs(
        onProgress: suspend (imported: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Int> {
        val token = authManager.getValidAccessToken()
            ?: return Result.failure(Exception("Not authenticated with Spotify"))

        return try {
            var offset = 0
            val pageSize = 50
            var totalTracks = 0
            var importedCount = 0

            // Fetch first page to get total
            val firstPage = spotifyApi.getLikedSongs(
                authHeader = token,
                limit = pageSize,
                offset = 0
            )
            totalTracks = firstPage.total
            onProgress(0, totalTracks)

            // Process first page
            val firstBatch = firstPage.items.mapNotNull { item ->
                item.track?.let { spotifyTrack ->
                    TrackEntity(
                        id = spotifyTrack.id,
                        title = spotifyTrack.name,
                        artist = spotifyTrack.artists.joinToString(", ") { it.name },
                        album = spotifyTrack.album?.name ?: "Unknown Album",
                        albumArtUrl = spotifyTrack.album?.images?.firstOrNull()?.url,
                        durationMs = spotifyTrack.durationMs,
                        isrc = spotifyTrack.externalIds?.isrc,
                        spotifyUri = "spotify:track:${spotifyTrack.id}"
                    )
                }
            }
            trackDao.insertTracks(firstBatch)
            importedCount += firstBatch.size
            offset += pageSize
            onProgress(importedCount, totalTracks)

            // Fetch remaining pages
            while (offset < totalTracks) {
                val page = spotifyApi.getLikedSongs(
                    authHeader = token,
                    limit = pageSize,
                    offset = offset
                )

                val batch = page.items.mapNotNull { item ->
                    item.track?.let { spotifyTrack ->
                        TrackEntity(
                            id = spotifyTrack.id,
                            title = spotifyTrack.name,
                            artist = spotifyTrack.artists.joinToString(", ") { it.name },
                            album = spotifyTrack.album?.name ?: "Unknown Album",
                            albumArtUrl = spotifyTrack.album?.images?.firstOrNull()?.url,
                            durationMs = spotifyTrack.durationMs,
                            isrc = spotifyTrack.externalIds?.isrc,
                            spotifyUri = "spotify:track:${spotifyTrack.id}"
                        )
                    }
                }
                trackDao.insertTracks(batch)
                importedCount += batch.size
                offset += pageSize
                onProgress(importedCount, totalTracks)
            }

            // Fetch and save user profile
            try {
                val profile = spotifyApi.getCurrentUser(token)
                profile.displayName?.let { authManager.saveUserDisplayName(it) }
            } catch (_: Exception) { /* Non-critical */ }

            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Import all user playlists from Spotify.
     * Fetches playlists, then iterates each playlist's tracks.
     *
     * @param onPlaylistProgress Called with (playlistName, imported, total)
     * @return Total number of unique tracks imported across all playlists
     */
    suspend fun importAllPlaylists(
        onPlaylistProgress: suspend (playlistName: String, imported: Int, total: Int) -> Unit = { _, _, _ -> }
    ): Result<Int> {
        val token = authManager.getValidAccessToken()
            ?: return Result.failure(Exception("Not authenticated with Spotify"))

        return try {
            var totalImported = 0
            var playlistOffset = 0
            val playlistPageSize = 50

            // Fetch playlists, paginated
            do {
                val playlistPage = spotifyApi.getUserPlaylists(
                    authHeader = token,
                    limit = playlistPageSize,
                    offset = playlistOffset
                )

                for (playlist in playlistPage.items) {
                    val playlistTotal = playlist.tracks?.total ?: 0
                    if (playlistTotal == 0) continue

                    var trackOffset = 0
                    var playlistImported = 0

                    while (trackOffset < playlistTotal) {
                        val tracksPage = spotifyApi.getPlaylistTracks(
                            authHeader = token,
                            playlistId = playlist.id,
                            limit = 100,
                            offset = trackOffset
                        )

                        val batch = tracksPage.items.mapNotNull { item ->
                            item.track?.let { spotifyTrack ->
                                TrackEntity(
                                    id = spotifyTrack.id,
                                    title = spotifyTrack.name,
                                    artist = spotifyTrack.artists.joinToString(", ") { it.name },
                                    album = spotifyTrack.album?.name ?: "Unknown Album",
                                    albumArtUrl = spotifyTrack.album?.images?.firstOrNull()?.url,
                                    durationMs = spotifyTrack.durationMs,
                                    isrc = spotifyTrack.externalIds?.isrc,
                                    spotifyUri = "spotify:track:${spotifyTrack.id}"
                                )
                            }
                        }

                        trackDao.insertTracks(batch)
                        playlistImported += batch.size
                        totalImported += batch.size
                        trackOffset += 100

                        onPlaylistProgress(playlist.name, playlistImported, playlistTotal)
                    }
                }

                playlistOffset += playlistPageSize
            } while (playlistOffset < (playlistPage?.total ?: 0))

            Result.success(totalImported)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Full import: Liked Songs + All Playlists.
     */
    suspend fun importFullLibrary(
        onProgress: suspend (phase: String, imported: Int, total: Int) -> Unit = { _, _, _ -> }
    ): Result<Int> {
        var totalImported = 0

        // Phase 1: Liked Songs
        val likedResult = importLikedSongs { imported, total ->
            onProgress("Liked Songs", imported, total)
        }
        likedResult.onSuccess { totalImported += it }
        likedResult.onFailure { return Result.failure(it) }

        // Phase 2: Playlists
        val playlistResult = importAllPlaylists { name, imported, total ->
            onProgress(name, totalImported + imported, totalImported + total)
        }
        playlistResult.onSuccess { totalImported += it }
        playlistResult.onFailure { return Result.failure(it) }

        return Result.success(totalImported)
    }

    /**
     * Clear all imported tracks (re-import).
     */
    suspend fun clearAllTracks() {
        trackDao.deleteAllTracks()
    }
}
