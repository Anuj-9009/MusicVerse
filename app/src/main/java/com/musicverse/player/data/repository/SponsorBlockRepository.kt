package com.musicverse.player.data.repository

import android.util.Log
import com.musicverse.player.data.api.SponsorBlockApiService
import com.musicverse.player.data.api.SponsorSegment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SponsorBlock Repository
 *
 * Fetches skip segments for YouTube videos from the free, community-driven
 * SponsorBlock API (https://sponsor.ajay.app). Used to automatically skip
 * intros, outros, and off-topic music content so the YouTube version
 * plays like a clean studio track.
 */
@Singleton
class SponsorBlockRepository @Inject constructor(
    private val sponsorBlockApi: SponsorBlockApiService
) {
    companion object {
        private const val TAG = "SponsorBlockRepository"
        // Minimum intro duration to skip — avoids skipping legit fade-ins
        private const val MIN_SKIP_DURATION_SEC = 5.0f
    }

    /**
     * Returns a list of [SponsorSegment]s that should be automatically skipped.
     * Returns an empty list if the video has no segments or the API call fails.
     */
    suspend fun getSkipSegments(videoId: String): List<SponsorSegment> {
        return try {
            val segments = sponsorBlockApi.getSkipSegments(videoId)
            // Filter out very short segments (< 5s) to avoid false positives
            segments.filter { it.endSec - it.startSec >= MIN_SKIP_DURATION_SEC }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                // 404 = no segments exist for this video, totally normal
                Log.d(TAG, "No SponsorBlock segments for videoId=$videoId")
            } else {
                Log.e(TAG, "SponsorBlock API error: ${e.code()} for videoId=$videoId")
            }
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch SponsorBlock segments for videoId=$videoId", e)
            emptyList()
        }
    }

    /**
     * Given the current playback position (in ms), check if we are inside a
     * skip segment. If we are, returns the endTimeSec to seek to. Otherwise null.
     */
    fun getSkipTarget(positionMs: Long, segments: List<SponsorSegment>): Long? {
        val positionSec = positionMs / 1000f
        val activeSegment = segments.firstOrNull { seg ->
            positionSec >= seg.startSec && positionSec < seg.endSec
        }
        return activeSegment?.let { (it.endSec * 1000).toLong() }
    }
}
