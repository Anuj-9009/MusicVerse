package com.musicverse.player.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * SponsorBlock API — Fetches skip segments for YouTube videos.
 * Free, open-source, community-driven database of YouTube sponsorships,
 * intros, outros, and off-topic music segments.
 * Base URL: https://sponsor.ajay.app/
 */
interface SponsorBlockApiService {

    /**
     * Get all skip segments for a given YouTube video.
     * Categories we care about for music:
     *   - "intro"         → Cinematic or talking intro before the music starts
     *   - "outro"         → Credits, cards, or fade-out after music ends
     *   - "music_offtask" → Non-music content in a music video
     *   - "sponsor"       → Channel self-promotion embedded in the video
     */
    @GET("api/skipSegments/{videoId}?categories=[\"intro\",\"outro\",\"music_offtask\",\"sponsor\"]")
    suspend fun getSkipSegments(@Path("videoId") videoId: String): List<SponsorSegment>
}
