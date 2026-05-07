package com.musicverse.player.data.api

import com.musicverse.player.data.models.YouTubeSearchResult
import com.musicverse.player.data.models.YouTubeVideoListResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * YouTube Data API v3 — Video Search & Details.
 *
 * Base URL: https://www.googleapis.com/youtube/v3/
 *
 * Used by the Version Discovery engine to find alternate
 * performances (live, acoustic, cover) of imported tracks.
 */
interface YouTubeApiService {

    /**
     * Search for videos matching a query.
     *
     * @param query   Search query (e.g., "Bohemian Rhapsody live concert")
     * @param apiKey  YouTube Data API key
     * @param maxResults  Number of results per page (max 50)
     * @param type    Resource type (always "video" for us)
     * @param videoCategoryId  10 = Music
     */
    @GET("search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 10,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("order") order: String = "relevance"
    ): YouTubeSearchResult

    /**
     * Get video details (duration, statistics, content details).
     *
     * @param ids  Comma-separated video IDs
     */
    @GET("videos")
    suspend fun getVideoDetails(
        @Query("id") ids: String,
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet,contentDetails,statistics"
    ): YouTubeVideoListResult
}
