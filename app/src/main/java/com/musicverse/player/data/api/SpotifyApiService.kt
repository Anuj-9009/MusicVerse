package com.musicverse.player.data.api

import com.musicverse.player.data.models.SpotifyTrackItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

/**
 * Spotify Web API — Liked Songs, Playlists & User Profile.
 *
 * Base URL: https://api.spotify.com/v1/
 */
interface SpotifyApiService {

    /**
     * Fetch user's saved tracks (Liked Songs).
     * Returns paginated results with ISRC in external_ids.
     */
    @GET("me/tracks")
    suspend fun getLikedSongs(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPaginatedResponse

    /**
     * Fetch user's playlists.
     */
    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPlaylistsResponse

    /**
     * Fetch tracks from a specific playlist.
     */
    @GET("playlists/{playlistId}/tracks")
    suspend fun getPlaylistTracks(
        @Header("Authorization") authHeader: String,
        @Path("playlistId") playlistId: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "items(track(id,name,artists,album,duration_ms,external_ids)),total,limit,offset,next"
    ): SpotifyPaginatedResponse

    /**
     * Fetch current user's profile.
     */
    @GET("me")
    suspend fun getCurrentUser(
        @Header("Authorization") authHeader: String
    ): SpotifyUserProfile
}

/**
 * Spotify Auth API — Token exchange & refresh.
 *
 * Base URL: https://accounts.spotify.com/
 */
interface SpotifyAuthApi {

    /**
     * Exchange authorization code for access + refresh tokens.
     */
    @FormUrlEncoded
    @POST("api/token")
    suspend fun exchangeCodeForToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): SpotifyTokenResponse

    /**
     * Refresh an expired access token.
     */
    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): SpotifyTokenResponse
}

// ── Response Models ──

@Serializable
data class SpotifyPaginatedResponse(
    val items: List<SpotifyTrackItem> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0,
    val next: String? = null,
    val previous: String? = null
)

@Serializable
data class SpotifyPlaylistsResponse(
    val items: List<SpotifyPlaylistItem> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0,
    val next: String? = null
)

@Serializable
data class SpotifyPlaylistItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val images: List<SpotifyProfileImage> = emptyList(),
    val tracks: SpotifyPlaylistTracksRef? = null,
    val owner: SpotifyPlaylistOwner? = null
)

@Serializable
data class SpotifyPlaylistTracksRef(
    val total: Int = 0,
    val href: String? = null
)

@Serializable
data class SpotifyPlaylistOwner(
    val id: String = "",
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class SpotifyTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresIn: Int = 3600,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val scope: String? = null
)

@Serializable
data class SpotifyUserProfile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val images: List<SpotifyProfileImage> = emptyList()
)

@Serializable
data class SpotifyProfileImage(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)
